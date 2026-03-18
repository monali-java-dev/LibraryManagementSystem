package com.library;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.net.URL;

// 🔹 पारदर्शक बटण क्लास
class TransparentButton extends JButton {
    public TransparentButton(String text) {
        super(text);
        setContentAreaFilled(false);  // बॅकग्राउंड पारदर्शक
        setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // पांढरी बॉर्डर
        setFocusPainted(false);
        setForeground(Color.WHITE);   // मजकुराचा रंग पांढरा
        setFont(new Font("Segoe UI", Font.BOLD, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // माऊस नेल्यावर हात दाखवेल
        setPreferredSize(new Dimension(250, 45));  // बटणचा आकार
    }
}

public class LibraryManagement {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Library Management System");
        frame.setSize(800, 600); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 🔹 इमेज लोड करण्याची खात्रीशीर पद्धत (getResource)
        URL imgUrl = LibraryManagement.class.getResource("/library.png");
        
        if (imgUrl != null) {
            ImageIcon bgIcon = new ImageIcon(imgUrl);
            Image bgImage = bgIcon.getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH);
            JLabel background = new JLabel(new ImageIcon(bgImage));
            
            // GridBagLayout मुळे बटन्स बरोबर सेंटरला येतील
            background.setLayout(new GridBagLayout()); 
            frame.setContentPane(background);

            // 🔹 बटन्ससाठी पॅनेल
            JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 15, 15)); 
            buttonPanel.setOpaque(false); // पॅनेल पारदर्शक ठेवणे
            buttonPanel.setPreferredSize(new Dimension(250, 300)); // पॅनेल size adjust

            // बटन्स तयार करणे
            JButton addBook = new TransparentButton("Add Book");
            JButton viewBooks = new TransparentButton("View Books");
            JButton addUser = new TransparentButton("Add User");
            JButton borrowBook = new TransparentButton("Borrow Book");
            JButton returnBook = new TransparentButton("Return Book");

            // पॅनेलमध्ये बटन्स टाकणे
            buttonPanel.add(addBook);
            buttonPanel.add(viewBooks);
            buttonPanel.add(addUser);
            buttonPanel.add(borrowBook);
            buttonPanel.add(returnBook);

            // इमेजवर पॅनेल ॲड करणे (centered)
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            background.add(buttonPanel, gbc);

            // ================= फंक्शनॅलिटी (Action Listeners) =================

            addBook.addActionListener(e -> {
                try (Connection con = DbConnection.getConnection()) {
                    String id = JOptionPane.showInputDialog(frame, "Enter Book ID:");
                    String title = JOptionPane.showInputDialog(frame, "Enter Title:");
                    String author = JOptionPane.showInputDialog(frame, "Enter Author:");

                    if (id != null && title != null && author != null) {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO books VALUES (?, ?, ?, true)");
                        ps.setInt(1, Integer.parseInt(id));
                        ps.setString(2, title);
                        ps.setString(3, author);
                        ps.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "Book Added Successfully!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            });

            viewBooks.addActionListener(e -> {
                try (Connection con = DbConnection.getConnection()) {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM books");

                    StringBuilder data = new StringBuilder("--- Books List ---\n");
                    while (rs.next()) {
                        data.append("ID: ").append(rs.getInt(1))
                            .append(" | Title: ").append(rs.getString(2))
                            .append(" | Author: ").append(rs.getString(3))
                            .append(" | Available: ").append(rs.getBoolean(4) ? "Yes" : "No")
                            .append("\n");
                    }
                    
                    JTextArea textArea = new JTextArea(data.toString());
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    textArea.setEditable(false);
                    scrollPane.setPreferredSize(new Dimension(450, 300));
                    JOptionPane.showMessageDialog(frame, scrollPane, "Library Records", JOptionPane.INFORMATION_MESSAGE);
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            });

            addUser.addActionListener(e -> {
                try (Connection con = DbConnection.getConnection()) {
                    String id = JOptionPane.showInputDialog(frame, "Enter User ID:");
                    String name = JOptionPane.showInputDialog(frame, "Enter User Name:");

                    if (id != null && name != null) {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO users VALUES (?, ?)");
                        ps.setInt(1, Integer.parseInt(id));
                        ps.setString(2, name);
                        ps.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "User Added!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            });

            borrowBook.addActionListener(e -> {
                try (Connection con = DbConnection.getConnection()) {
                    String bid = JOptionPane.showInputDialog(frame, "Enter Book ID to Borrow:");
                    String uid = JOptionPane.showInputDialog(frame, "Enter Your User ID:");

                    if (bid != null && uid != null) {
                        PreparedStatement check = con.prepareStatement("SELECT available FROM books WHERE book_id=?");
                        check.setInt(1, Integer.parseInt(bid));
                        ResultSet rs = check.executeQuery();

                        if (rs.next() && rs.getBoolean(1)) {
                            con.setAutoCommit(false); 
                            
                            PreparedStatement ps1 = con.prepareStatement("UPDATE books SET available=false WHERE book_id=?");
                            ps1.setInt(1, Integer.parseInt(bid));
                            ps1.executeUpdate();

                            PreparedStatement ps2 = con.prepareStatement(
                                "INSERT INTO transactions (book_id, user_id, issue_date) VALUES (?, ?, CURDATE())"
                            );
                            ps2.setInt(1, Integer.parseInt(bid));
                            ps2.setInt(2, Integer.parseInt(uid));
                            ps2.executeUpdate();

                            con.commit();
                            JOptionPane.showMessageDialog(frame, "Book Borrowed Successfully!");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Book is not available or ID incorrect.");
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            });

            returnBook.addActionListener(e -> {
                try (Connection con = DbConnection.getConnection()) {
                    String bid = JOptionPane.showInputDialog(frame, "Enter Book ID to Return:");

                    if (bid != null) {
                        PreparedStatement check = con.prepareStatement(
                            "SELECT * FROM transactions WHERE book_id=? AND return_date IS NULL"
                        );
                        check.setInt(1, Integer.parseInt(bid));
                        ResultSet rs = check.executeQuery();

                        if (rs.next()) {
                            con.setAutoCommit(false);
                            
                            PreparedStatement ps1 = con.prepareStatement("UPDATE books SET available=true WHERE book_id=?");
                            ps1.setInt(1, Integer.parseInt(bid));
                            ps1.executeUpdate();

                            PreparedStatement ps2 = con.prepareStatement(
                                "UPDATE transactions SET return_date=CURDATE() WHERE book_id=? AND return_date IS NULL"
                            );
                            ps2.setInt(1, Integer.parseInt(bid));
                            ps2.executeUpdate();

                            con.commit();
                            JOptionPane.showMessageDialog(frame, "Book Returned Successfully!");
                        } else {
                            JOptionPane.showMessageDialog(frame, "This book was not borrowed or ID incorrect.");
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            });

        } else {
            // इमेज सापडली नाही तर साधा बॅकग्राउंड आणि एरर मेसेज
            frame.getContentPane().setBackground(Color.DARK_GRAY);
            JOptionPane.showMessageDialog(frame, "इमेज 'library.png' सापडली नाही. कृपया ती 'src' फोल्डरमध्ये आहे का ते तपासा.");
        }

        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);
    }
}