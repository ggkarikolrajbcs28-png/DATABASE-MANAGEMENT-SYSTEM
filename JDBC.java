package dbms;

import java.awt.*;
import java.sql.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class EmployeeExperienceApp extends JFrame {

    JTextField idField, mobileField, experienceField;
    JButton experienceBtn;
    JTable table;
    DefaultTableModel model;

    Connection con;

    static class EmployeeRecord {
        final int empId;
        final String empName;
        final String mobile;
        final String address;
        final String joiningDate;
        final String empStatus;
        final int empAge;
        final String empRole;

        EmployeeRecord(int empId, String empName, String mobile, String address, String joiningDate,
                       String empStatus, int empAge, String empRole) {
            this.empId = empId;
            this.empName = empName;
            this.mobile = mobile;
            this.address = address;
            this.joiningDate = joiningDate;
            this.empStatus = empStatus;
            this.empAge = empAge;
            this.empRole = empRole;
        }
    }

    EmployeeRecord fetchEmployeeById(int empId) {
        if (con == null) {
            return null;
        }
        String sql = "SELECT employee_id, employee_name, mobile, address, joining_date, employment_status, emp_age, emp_role FROM employee WHERE employee_id=?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, empId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new EmployeeRecord(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getInt(7),
                            rs.getString(8));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    void printEmployeeDetails(int empId, String empName, String mobile, String address, String joiningDate,
                              String empStatus, int empAge, String empRole, String actionLine) {
        System.out.println("Employee ID : " + empId);
        System.out.println("Name : " + empName);
        System.out.println("Mobile : " + mobile);
        System.out.println("Address : " + address);
        System.out.println("Joining Date : " + joiningDate);
        System.out.println("Status : " + empStatus);
        System.out.println("Age : " + empAge);
        System.out.println("Role : " + empRole);
        if (actionLine != null && !actionLine.isEmpty()) {
            System.out.println(actionLine);
        }
    }

    void printEmployeeById(int empId, String actionLineIfFound) {
        if (con == null) {
            return;
        }
        String sql = "SELECT employee_id, employee_name, mobile, address, joining_date, employment_status, emp_age, emp_role FROM employee WHERE employee_id=?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, empId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    printEmployeeDetails(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getInt(7),
                            rs.getString(8),
                            actionLineIfFound);
                } else {
                    System.out.println("Employee with ID " + empId + " not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void printAllEmployees() {
        if (con == null) {
            return;
        }
        String sql = "SELECT employee_id, employee_name, mobile, address, joining_date, employment_status, emp_age, emp_role FROM employee ORDER BY employee_id";
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- ALL EMPLOYEES ---");
            while (rs.next()) {
                int empId = rs.getInt(1);
                String empName = rs.getString(2);
                String mobile = rs.getString(3);
                String address = rs.getString(4);
                String joiningDate = rs.getString(5);
                String empStatus = rs.getString(6);
                int empAge = rs.getInt(7);
                String empRole = rs.getString(8);
                System.out.println(
                        "ID : " + empId + ", Name : " + empName + ", Mobile : " + mobile +
                        ", Role : " + empRole + ", Status : " + empStatus);
            }
            System.out.println("--------------------\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    EmployeeExperienceApp() {
        setTitle("Employee Management - Oracle");
        setLayout(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem insertItem = new JMenuItem("Insert");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem updateItem = new JMenuItem("Update");
        menu.add(insertItem);
        menu.add(deleteItem);
        menu.add(updateItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        JLabel idLabel = new JLabel("Enter Employee ID:");
        idLabel.setBounds(50, 30, 200, 30);
        add(idLabel);

        idField = new JTextField();
        idField.setBounds(220, 30, 200, 30);
        add(idField);

        // Experience button (replaces Search button)
        experienceBtn = new JButton("Experience");
        experienceBtn.setBounds(50, 80, 150, 35);
        add(experienceBtn);

        // Label and text box to display experience years
        JLabel expLabel = new JLabel("Experience (Years):");
        expLabel.setBounds(220, 80, 160, 35);
        add(expLabel);

        experienceField = new JTextField();
        experienceField.setBounds(390, 80, 100, 35);
        experienceField.setEditable(false);
        add(experienceField);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] { "ID", "Name", "Mobile", "Address", "Joining Date", "Status", "Age", "Role" });

        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(50, 150, 1200, 500);
        add(sp);

        connectDB();
        if (con != null) {
            loadTable();
        } else {
            experienceBtn.setEnabled(false);
            insertItem.setEnabled(false);
            deleteItem.setEnabled(false);
            updateItem.setEnabled(false);
        }

        experienceBtn.addActionListener(e -> calculateExperience());
        insertItem.addActionListener(e -> new InsertForm());
        deleteItem.addActionListener(e -> deleteEmployee());
        updateItem.addActionListener(e -> new UpdateForm());

        setVisible(true);

        Thread terminalThread = new Thread(this::runTerminalMenu, "terminal-menu-thread");
        terminalThread.setDaemon(true);
        terminalThread.start();
    }

    // NEW: Calculate and display experience in years
    void calculateExperience() {
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Not connected to DB.");
            return;
        }
        try {
            int empId = Integer.parseInt(idField.getText().trim());

            String sql = "SELECT TRUNC(MONTHS_BETWEEN(SYSDATE, joining_date) / 12) FROM employee WHERE employee_id = ?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, empId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        int years = rs.getInt(1);
                        experienceField.setText(years + " yrs");
                        printEmployeeById(empId, "Experience: " + years + " year(s).");
                    } else {
                        experienceField.setText("");
                        JOptionPane.showMessageDialog(this, "Employee ID " + empId + " not found.");
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Employee ID!");
            idField.setText("");
            experienceField.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Query Error: " + e.getMessage());
        }
    }

    void runTerminalMenu() {
        if (con == null) {
            System.out.println("DB not connected. Terminal menu disabled.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("\n--- TERMINAL MENU ---");
                System.out.println("1. Insert Employee");
                System.out.println("2. Update Employee");
                System.out.println("3. Delete Employee");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");

                String choice = sc.nextLine().trim();
                if ("1".equals(choice)) {
                    terminalInsert(sc);
                } else if ("2".equals(choice)) {
                    terminalUpdate(sc);
                } else if ("3".equals(choice)) {
                    terminalDelete(sc);
                } else if ("4".equals(choice)) {
                    System.out.println("Exiting terminal menu.");
                    break;
                } else {
                    System.out.println("Invalid choice. Enter 1, 2, 3, or 4.");
                    continue;
                }
            } catch (Exception ex) {
                System.out.println("Operation failed: " + ex.getMessage());
            }
        }
    }

    void terminalInsert(Scanner sc) {
        try {
            System.out.print("Enter Employee ID: ");
            int empId = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Enter Name: ");
            String empName = sc.nextLine().trim();

            System.out.print("Enter Mobile: ");
            String mobile = sc.nextLine().trim();

            System.out.print("Enter Address: ");
            String address = sc.nextLine().trim();

            System.out.print("Enter Joining Date (DD-MON-YY): ");
            String joiningDate = sc.nextLine().trim();

            System.out.print("Enter Status (Active/Inactive): ");
            String empStatus = sc.nextLine().trim();

            System.out.print("Enter Age: ");
            int empAge = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Enter Role: ");
            String empRole = sc.nextLine().trim();

            PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO employee (employee_id, employee_name, mobile, address, joining_date, employment_status, emp_age, emp_role) VALUES(?,?,?,?,TO_DATE(?,'DD-MON-YY'),?,?,?)");

            pst.setInt(1, empId);
            pst.setString(2, empName);
            pst.setString(3, mobile);
            pst.setString(4, address);
            pst.setString(5, joiningDate);
            pst.setString(6, empStatus);
            pst.setInt(7, empAge);
            pst.setString(8, empRole);

            pst.executeUpdate();

            printEmployeeDetails(empId, empName, mobile, address, joiningDate, empStatus, empAge, empRole, "inserted into DB.");
            printAllEmployees();
            loadTable();

        } catch (Exception ex) {
            System.out.println("Insert failed: " + ex.getMessage());
        }
    }

    void terminalUpdate(Scanner sc) {
        try {
            System.out.print("Enter Employee ID to update: ");
            int empId = Integer.parseInt(sc.nextLine().trim());

            EmployeeRecord loaded = fetchEmployeeById(empId);
            if (loaded == null) {
                System.out.println("Employee not found!");
                return;
            }

            System.out.println("\nCurrent Details:");
            printEmployeeDetails(loaded.empId, loaded.empName, loaded.mobile, loaded.address,
                                loaded.joiningDate, loaded.empStatus, loaded.empAge, loaded.empRole, "");

            System.out.print("\nEnter new Name (or press Enter to keep): ");
            String newName = sc.nextLine().trim();
            if (newName.isEmpty()) newName = loaded.empName;

            System.out.print("Enter new Mobile (or press Enter to keep): ");
            String newMobile = sc.nextLine().trim();
            if (newMobile.isEmpty()) newMobile = loaded.mobile;

            System.out.print("Enter new Address (or press Enter to keep): ");
            String newAddress = sc.nextLine().trim();
            if (newAddress.isEmpty()) newAddress = loaded.address;

            System.out.print("Enter new Joining Date (DD-MON-YY) (or press Enter to keep): ");
            String newJoiningDate = sc.nextLine().trim();
            if (newJoiningDate.isEmpty()) newJoiningDate = loaded.joiningDate;

            System.out.print("Enter new Status (or press Enter to keep): ");
            String newStatus = sc.nextLine().trim();
            if (newStatus.isEmpty()) newStatus = loaded.empStatus;

            System.out.print("Enter new Age (or press Enter to keep): ");
            String newAgeStr = sc.nextLine().trim();
            int newAge = newAgeStr.isEmpty() ? loaded.empAge : Integer.parseInt(newAgeStr);

            System.out.print("Enter new Role (or press Enter to keep): ");
            String newRole = sc.nextLine().trim();
            if (newRole.isEmpty()) newRole = loaded.empRole;

            PreparedStatement pst = con.prepareStatement(
                    "UPDATE employee SET employee_name=?, mobile=?, address=?, joining_date=TO_DATE(?,'DD-MON-YY'), employment_status=?, emp_age=?, emp_role=? WHERE employee_id=?");

            pst.setString(1, newName);
            pst.setString(2, newMobile);
            pst.setString(3, newAddress);
            pst.setString(4, newJoiningDate);
            pst.setString(5, newStatus);
            pst.setInt(6, newAge);
            pst.setString(7, newRole);
            pst.setInt(8, empId);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                printEmployeeById(empId, "updated in DB.");
                printAllEmployees();
                loadTable();
            } else {
                System.out.println("Employee not found!");
            }

        } catch (Exception ex) {
            System.out.println("Update failed: " + ex.getMessage());
        }
    }

    void terminalDelete(Scanner sc) {
        try {
            System.out.print("Enter Employee ID to delete: ");
            int empId = Integer.parseInt(sc.nextLine().trim());

            printEmployeeById(empId, "will be deleted...");

            System.out.print("Are you sure? (yes/no): ");
            String confirm = sc.nextLine().trim();

            if ("yes".equalsIgnoreCase(confirm)) {
                PreparedStatement pst = con.prepareStatement("DELETE FROM employee WHERE employee_id=?");
                pst.setInt(1, empId);
                int rows = pst.executeUpdate();

                if (rows > 0) {
                    System.out.println("deleted from DB.");
                    printAllEmployees();
                    loadTable();
                } else {
                    System.out.println("Employee not found!");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }

        } catch (Exception ex) {
            System.out.println("Delete failed: " + ex.getMessage());
        }
    }

    void connectDB() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");

            String url = "jdbc:oracle:thin:@localhost:1521:xe";
            String user = "system";
            String pass = "system123";

            con = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to Oracle DB");

        } catch (Exception e) {
            con = null;
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "DB Connection Failed:\n" + e,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void loadTable() {
        if (con == null) {
            return;
        }
        try {
            model.setRowCount(0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT employee_id, employee_name, mobile, address, joining_date, employment_status, emp_age, emp_role FROM employee");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getInt(7),
                        rs.getString(8)
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class InsertForm extends JFrame {
        JTextField empId, empName, mobile, address, joiningDate, empStatus, empAge, empRole;

        InsertForm() {
            setTitle("Insert Employee");
            setSize(450, 450);
            setLayout(new GridLayout(9, 2, 10, 10));
            setLocationRelativeTo(null);

            empId = new JTextField();
            empName = new JTextField();
            mobile = new JTextField();
            address = new JTextField();
            joiningDate = new JTextField();
            empStatus = new JTextField();
            empAge = new JTextField();
            empRole = new JTextField();

            add(new JLabel("Employee ID"));
            add(empId);
            add(new JLabel("Employee Name"));
            add(empName);
            add(new JLabel("Mobile"));
            add(mobile);
            add(new JLabel("Address"));
            add(address);
            add(new JLabel("Joining Date (DD-MON-YY)"));
            add(joiningDate);
            add(new JLabel("Status (Active/Inactive)"));
            add(empStatus);
            add(new JLabel("Age"));
            add(empAge);
            add(new JLabel("Role"));
            add(empRole);

            JButton save = new JButton("Save");
            add(save);

            save.addActionListener(e -> {
                try {
                    if (con == null) {
                        JOptionPane.showMessageDialog(this, "Not connected to DB.");
                        return;
                    }

                    int id = Integer.parseInt(empId.getText());
                    String name = empName.getText();
                    String mob = mobile.getText();
                    String addr = address.getText();
                    String joinDate = joiningDate.getText();
                    String status = empStatus.getText();
                    int age = Integer.parseInt(empAge.getText());
                    String role = empRole.getText();

                    PreparedStatement pst = con.prepareStatement(
                            "INSERT INTO employee (employee_id, employee_name, mobile, address, joining_date, employment_status, emp_age, emp_role) VALUES(?,?,?,?,TO_DATE(?,'DD-MON-YY'),?,?,?)");

                    pst.setInt(1, id);
                    pst.setString(2, name);
                    pst.setString(3, mob);
                    pst.setString(4, addr);
                    pst.setString(5, joinDate);
                    pst.setString(6, status);
                    pst.setInt(7, age);
                    pst.setString(8, role);

                    pst.executeUpdate();

                    printEmployeeDetails(id, name, mob, addr, joinDate, status, age, role, "inserted into DB.");

                    loadTable();
                    printAllEmployees();
                    dispose();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Insert Error!");
                }
            });

            setVisible(true);
        }
    }

    void deleteEmployee() {
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Not connected to DB.");
            return;
        }
        String id = JOptionPane.showInputDialog("Enter Employee ID to delete");
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?");
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int empId = Integer.parseInt(id.trim());
                printEmployeeById(empId, "will be deleted...");

                PreparedStatement pst = con.prepareStatement(
                        "DELETE FROM employee WHERE employee_id=?");

                pst.setInt(1, empId);
                int rows = pst.executeUpdate();

                if (rows > 0) {
                    System.out.println("deleted from DB.");
                    printAllEmployees();
                } else {
                    JOptionPane.showMessageDialog(this, "Employee not found!");
                }

                loadTable();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Delete Error!");
            }
        }
    }

    class UpdateForm extends JFrame {
        JTextField empId, empName, mobile, address, joiningDate, empStatus, empAge, empRole;
        EmployeeRecord loaded;

        UpdateForm() {
            setTitle("Update Employee");
            setSize(500, 450);
            setLayout(new GridLayout(10, 2, 10, 10));
            setLocationRelativeTo(null);

            empId = new JTextField();

            add(new JLabel("Enter Employee ID"));
            add(empId);

            JButton fetch = new JButton("Fetch");
            add(new JLabel(""));
            add(fetch);

            empName = new JTextField();
            mobile = new JTextField();
            address = new JTextField();
            joiningDate = new JTextField(); // format: YYYY-MM-DD
            empStatus = new JTextField();
            empAge = new JTextField();
            empRole = new JTextField();

            // disable initially
            empName.setEnabled(false);
            mobile.setEnabled(false);
            address.setEnabled(false);
            joiningDate.setEnabled(false);
            empStatus.setEnabled(false);
            empAge.setEnabled(false);
            empRole.setEnabled(false);

            add(new JLabel("Name")); add(empName);
            add(new JLabel("Mobile")); add(mobile);
            add(new JLabel("Address")); add(address);
            add(new JLabel("Joining Date (YYYY-MM-DD)")); add(joiningDate);
            add(new JLabel("Status")); add(empStatus);
            add(new JLabel("Age")); add(empAge);
            add(new JLabel("Role")); add(empRole);

            JButton update = new JButton("Update");
            update.setEnabled(false);
            add(new JLabel(""));
            add(update);

            // 🔍 FETCH BUTTON
            fetch.addActionListener(e -> {
                try {
                    int id = Integer.parseInt(empId.getText().trim());

                    loaded = fetchEmployeeById(id);
                    if (loaded == null) {
                        JOptionPane.showMessageDialog(this, "Employee not found!");
                        return;
                    }

                    empId.setEnabled(false);

                    empName.setEnabled(true);
                    mobile.setEnabled(true);
                    address.setEnabled(true);
                    joiningDate.setEnabled(true);
                    empStatus.setEnabled(true);
                    empAge.setEnabled(true);
                    empRole.setEnabled(true);

                    empName.setText(loaded.empName);
                    mobile.setText(loaded.mobile);
                    address.setText(loaded.address);

                    // IMPORTANT: convert to YYYY-MM-DD
                    String dateOnly = loaded.joiningDate.split(" ")[0];
                    empJoiningDateSafe(dateOnly);

                    empStatus.setText(loaded.empStatus);
                    empAge.setText(String.valueOf(loaded.empAge));
                    empRole.setText(loaded.empRole == null ? "" : loaded.empRole);

                    update.setEnabled(true);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid ID!");
                }
            });

            // 🔥 UPDATE BUTTON (FIXED)
            update.addActionListener(e -> {
                try {
                    int id = loaded.empId;

                    String newName = empName.getText().trim();
                    if (newName.isEmpty()) newName = loaded.empName;

                    String newMobile = mobile.getText().trim();
                    if (newMobile.isEmpty()) newMobile = loaded.mobile;

                    String newAddress = address.getText().trim();
                    if (newAddress.isEmpty()) newAddress = loaded.address;

                    String newJoiningDate = joiningDate.getText().trim();
                    if (newJoiningDate.isEmpty())
                        newJoiningDate = loaded.joiningDate.split(" ")[0];

                    String newStatus = empStatus.getText().trim();
                    if (newStatus.isEmpty()) newStatus = loaded.empStatus;

                    int newAge;
                    try {
                        newAge = empAge.getText().trim().isEmpty() ? loaded.empAge
                                : Integer.parseInt(empAge.getText().trim());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Invalid Age!");
                        return;
                    }

                    String newRole = empRole.getText().trim();
                    if (newRole.isEmpty()) newRole = "worker"; // default

                    // ✅ Convert date safely
                    java.sql.Date sqlDate = java.sql.Date.valueOf(newJoiningDate);

                    PreparedStatement pst = con.prepareStatement(
                        "UPDATE employee SET employee_name=?, mobile=?, address=?, joining_date=?, employment_status=?, emp_age=?, emp_role=? WHERE employee_id=?"
                    );

                    pst.setString(1, newName);
                    pst.setString(2, newMobile);
                    pst.setString(3, newAddress);
                    pst.setDate(4, sqlDate); // FIXED
                    pst.setString(5, newStatus);
                    pst.setInt(6, newAge);
                    pst.setString(7, newRole);
                    pst.setInt(8, id);

                    int rows = pst.executeUpdate();

                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Updated Successfully!");
                        printEmployeeById(id, "updated in DB.");
                        printAllEmployees();
                        loadTable();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Update Failed!");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Update Error: " + ex.getMessage());
                }
            });

            setVisible(true);
        }

        // helper method
        void empJoiningDateSafe(String dateOnly) {
            try {
                joiningDate.setText(dateOnly);
            } catch (Exception e) {
                joiningDate.setText("");
            }
        }
    }

    public static void main(String[] args) {
        new EmployeeExperienceApp();
    }
}
