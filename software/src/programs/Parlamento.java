/*
 *
 *   ReBayCT: a tool for classification on a Thesaurus
 *
 *	For details see:
 *	 L. M. de Campos, A. E. Romero, Bayesian Network Models for Hierarchical Text 
 *	Classification from a Thesaurus, Int. J. Approx. Reasoning 50(7): 932-944 (2009).
 *
 *
 *   Copyright (C) 2006-2008 Alfonso E. Romero <alfonsoeromero (AT) gmail (DOT) com>
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * Parlamento.java
 *
 * Created on 26 de octubre de 2006, 2:18
 */

package programs;

import java.util.*;
import unsupervisedClassifier.*;

/**
 *
 * @author  aeromero
 */
public class Parlamento extends javax.swing.JFrame {
    
    ClassifierModel cm;
    
    /** Creates new form Parlamento */
    public Parlamento() {
        
        initComponents();
        this.setLocationRelativeTo(null);
        this.setExtendedState(this.getExtendedState() | javax.swing.JFrame.MAXIMIZED_BOTH);
        // replace this with your favorite "thesaurus index" and "stopwords list"
        cm = new BNClassifier("congreso1", "otra.txt", true, true, true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(":: Clasificaci\u00f3n autom\u00e1tica de iniciativas ::");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setLocationByPlatform(true);
        setMaximizedBounds(new java.awt.Rectangle(0, 0, 800, 600));
        jPanel1.setLayout(new java.awt.BorderLayout(2, 2));

        jPanel1.setMaximumSize(new java.awt.Dimension(2000, 2000));
        jPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel2.setLayout(new java.awt.GridLayout());

        jLabel1.setText("Texto de la iniciativa:");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jPanel2.add(jLabel1);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(2);
        jTextArea1.setMaximumSize(new java.awt.Dimension(10, 10));
        jTextArea1.setPreferredSize(new java.awt.Dimension(100, 30));
        jScrollPane1.setViewportView(jTextArea1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jButton1.setText("Clasificar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton1);

        jPanel1.add(jPanel3, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jTextArea2.setColumns(20);
        jTextArea2.setEditable(false);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jPanel4.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// TODO add your handling code here:
        if (this.jTextArea1.getText().length() > 0)
        {
          String s = this.jTextArea1.getText();
          List<Result> lr = this.cm.classifyString(s);
          this.jTextArea2.setText("");
          this.jTextArea2.repaint();
          
          StringBuilder sb = new StringBuilder("");
          for (Result r : lr)
            sb.append(r.getDescriptor().getDescription()).append(" ").append(r.getProb()).append("\n");
          
          this.jTextArea2.setText(sb.toString());
          this.jTextArea2.repaint(); 
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
                   
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Parlamento().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    // End of variables declaration//GEN-END:variables
    
}
