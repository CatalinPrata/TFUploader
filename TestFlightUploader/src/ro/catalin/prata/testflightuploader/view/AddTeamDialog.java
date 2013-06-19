package ro.catalin.prata.testflightuploader.view;

import com.intellij.ui.JBColor;
import ro.catalin.prata.testflightuploader.Model.Team;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Used to display two text fields for the team name and token,
 * it also contains a callback mechanism to notify the user of this dialog when a new team was added
 */
public class AddTeamDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    /**
     * A default text field border, used to set the alert bordered text field back to normal
     */
    private Border defaultTextFieldBorder;
    /**
     * Contains the team name text
     */
    private JTextField teamNameTextField;
    /**
     * Contains the team token text
     */
    private JTextField teamToken;
    private JLabel teamTokenLabel;
    /**
     * Used to notify the caller when a new team is added
     */
    private AddTeamListener listener;

    /**
     * Constructor used to create a dialog and set a callback to it
     *
     * @param listener listens for new team addition on this dialog
     */
    public AddTeamDialog(AddTeamListener listener) {

        // set the callback
        this.listener = listener;

        setLocationRelativeTo(null);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // set the token label text
        teamTokenLabel.setText("<HTML>Team token, see <a href=\"https://testflightapp.com/dashboard/team/edit/?next=/api/doc/\">here.</a></HTML>");
        teamTokenLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        goToWebsite(teamTokenLabel);

        // save a default border for latter
        defaultTextFieldBorder = teamNameTextField.getBorder();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * Callback mechanism used to notify when a new team was added
     *
     * @param listener callback used to notify the new team addition
     */
    public void setListener(AddTeamListener listener) {
        this.listener = listener;
    }

    private void onOK() {

        // create a team object
        Team team = new Team();

        // check if we have a valid team name
        if (teamNameTextField.getText() != null && teamNameTextField.getText().length() > 1) {
            team.setName(teamNameTextField.getText());
        } else {
            setAlertBorderToTextField(teamNameTextField);
            return;
        }

        // check if we have a valid token
        if (teamToken.getText() != null && teamToken.getText().length() > 1) {
            team.setToken(teamToken.getText());
        } else {
            setAlertBorderToTextField(teamToken);
            return;
        }

        if (listener != null) {
            // notify the user of this dialog that a new team was added, also send the new team
            listener.onTeamAdded(team);
        }

        dispose();
    }

    /**
     * Set the given Text Field a red border so the user can see that something is wrong
     *
     * @param textField the text field that needs to be colored
     */
    public void setAlertBorderToTextField(JTextField textField) {

        textField.setBorder(new LineBorder(JBColor.RED));

    }

    /**
     * Set the default border to the given text field
     *
     * @param textField the text field who's color should be turned back to defaults
     */
    public void setDefaultBorderToTextField(JTextField textField) {

        textField.setBorder(defaultTextFieldBorder);

    }

    private void goToWebsite(JLabel website) {
        website.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://testflightapp.com/dashboard/team/edit/?next=/api/doc/"));
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void onCancel() {
        // just close the dialog
        dispose();
    }

    /**
     * Callback used to send the new created team to the caller of this dialog
     */
    public interface AddTeamListener {

        /**
         * Called when a new team was added
         *
         * @param newTeam the new team containing a name and a token
         */
        public void onTeamAdded(Team newTeam);

    }

}
