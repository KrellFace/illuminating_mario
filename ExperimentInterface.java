package illumsearch;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class ExperimentInterface {


    //Instantiate objects we'll need
    JFrame mainFrame;
    JPanel mainPanel;

    protected static final int Algo_MapElites = 1;
    protected static final int Algo_Shine = 2;

    //Configuration types - Define level characteristics used in current runs
    protected static final int Config_JEvsBC = 1;
    protected static final int Config_JEvsWidth = 2;
    protected static final int Config_JEvsSpeed = 3;
    protected static final int Config_JEvsContig = 4;
    protected static final int Config_SpeedvsContig = 5;


    //Set destination of output folders here
    private static File Output_Location;

    private int defaultoffspring = 20000;

    private int algotype = Algo_MapElites;
    private int numberofruns = 1;
    private int numberoffspring = defaultoffspring;
    private int configtype = Config_JEvsBC;
    private String batchRunName;

    public static void main(String[] args) {


        ExperimentInterface ei = new ExperimentInterface();

        ei.run();

    }

    public void run() {


        mainFrame = new JFrame();
        mainPanel = new JPanel();

        //Setting the gridlayout of interface
        mainPanel.setLayout(new GridLayout(6, 1));

        JPanel outPanel = outLocPanel();
        mainPanel.add(outPanel);

        JPanel algoPanel = algoPanel();
        mainPanel.add(algoPanel);

        JPanel numrunPanel = numrunPanel();
        mainPanel.add(numrunPanel);

        JPanel totaloffspring = numoffspringPanel();
        mainPanel.add(totaloffspring);

        JPanel configpanel = runtypePanel();
        mainPanel.add(configpanel);


        JButton submitButton = new JButton("Initialize Experiment");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Form submitted. Initialising Experiments");
                System.out.println("Algorithm type: " + algotype + ". Num Runs: " + numberofruns + " Config type" + configtype);

                mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));

                for (int i = 0; i < numberofruns; i++) {

                    String runName = "\\Run" + (i + 1);

                    Path runPath = Paths.get(Output_Location + runName);
                    try {
                        Files.createDirectory(runPath);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    AlgorithmRun currRun = new AlgorithmRun(algotype, numberoffspring, configtype, runPath, runName);
                    currRun.run();
                    currRun = null;

                }

                System.exit(0);


            }
        });

        mainPanel.add(submitButton);

        //Adding panel to frame
        mainFrame.add(mainPanel);

        mainFrame.setSize(700, 700);
        mainFrame.setVisible(true);





    }

    public JPanel outLocPanel() {
        JPanel outLocPanel = new JPanel();
        JTextField currFolder = new JTextField();
        currFolder.setText("Current folder: NULL");
        currFolder.setEditable(false);
        JButton selectB = new JButton();
        selectB.setText("Choose Output Folder");
        selectB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File("."));
                chooser.setDialogTitle("Select Output Folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //
                // disable the "All files" option.
                //
                chooser.setAcceptAllFileFilterUsed(false);
                //    

                if (chooser.showOpenDialog(selectB) == JFileChooser.APPROVE_OPTION) {
                    System.out.println("getCurrentDirectory(): " +
                        chooser.getCurrentDirectory());
                    System.out.println("getSelectedFile() : " +
                        chooser.getSelectedFile());

                    Output_Location = chooser.getSelectedFile();
                    currFolder.setText("Current folder: " + Output_Location);
                } else {
                    System.out.println("No Selection ");
                }


            }
        });

        outLocPanel.add(selectB);
        outLocPanel.add(currFolder);

        return outLocPanel;

    }



    public JPanel algoPanel() {
        JPanel algoPanel = new JPanel();
        JTextField f1 = new JTextField();
        f1.setText("Select algorithm:");
        f1.setEditable(false);
        algoPanel.add(f1);
        JPanel f2 = new JPanel();
        CheckboxGroup algoGrp = new CheckboxGroup();
        Checkbox me = new Checkbox("MAP-Elites", algoGrp, true);
        me.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Map Elites Selected");
                algotype = Algo_MapElites;

            }
        });

        Checkbox shine = new Checkbox("SHINE", algoGrp, true);
        shine.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Shine selected");
                algotype = Algo_Shine;

            }
        });

        algoGrp.setSelectedCheckbox(me);

        f2.add(me);
        f2.add(shine);
        algoPanel.add(f2);
        return algoPanel;
    }

    public JPanel numrunPanel() {
        JPanel numrunPanel = new JPanel();
        JTextField f1 = new JTextField();
        f1.setText("Number of full runs: ");
        f1.setEditable(false);
        numrunPanel.add(f1);
        JPanel f2 = new JPanel();


        Choice runOpts = new Choice();
        for (int i = 0; i < 5; i++) {
            runOpts.add(String.valueOf(i + 1));
        }

        runOpts.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                numberofruns = Integer.parseInt(runOpts.getSelectedItem());
            }
        });


        f2.add(runOpts);
        numrunPanel.add(f2);
        return numrunPanel;


    }

    public JPanel numoffspringPanel() {
        JPanel numrunPanel = new JPanel();
        JTextField f1 = new JTextField();
        f1.setText("Number of offspring per run: ");
        f1.setEditable(false);
        numrunPanel.add(f1);

        //Storage for slider
        JPanel f2 = new JPanel();
        //Storage for current slider value
        JTextField f3 = new JTextField();
        JSlider offspringSlider = new JSlider(JSlider.HORIZONTAL, 0, 200000, defaultoffspring);
        offspringSlider.setMajorTickSpacing(10000);
        offspringSlider.setMinorTickSpacing(5000);
        offspringSlider.setPaintTicks(true);
        offspringSlider.setSnapToTicks(true);


        offspringSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                numberoffspring = offspringSlider.getValue();
                f3.setText(Integer.toString(offspringSlider.getValue()));
            }
        });

        f2.add(offspringSlider);
        numrunPanel.add(f2);


        f3.setText(Integer.toString(offspringSlider.getValue()));
        f3.setEditable(false);
        numrunPanel.add(f3);

        return numrunPanel;


    }

    public JPanel runtypePanel() {
        JPanel runtypePanel = new JPanel();
        JTextField f1 = new JTextField();
        f1.setText("Run Configuration:");
        f1.setEditable(false);
        runtypePanel.add(f1);
        JPanel f2 = new JPanel();
        CheckboxGroup rtGrp = new CheckboxGroup();

        Checkbox jevsbc = new Checkbox("Jump Entropy vs Block Count", rtGrp, true);
        jevsbc.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("JEvsBC Selected");
                configtype = Config_JEvsBC;

            }
        });

        Checkbox jevscontig = new Checkbox("Jump Entropy vs Contiguity Score", rtGrp, true);
        jevscontig.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("JE vs Contig selected");
                configtype = Config_JEvsContig;

            }
        });

        Checkbox jevsspeed = new Checkbox("Jump Entropy vs Speed", rtGrp, true);
        jevsspeed.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("JE vs Speed selected");
                configtype = Config_JEvsSpeed;

            }
        });

        Checkbox speedvscontig = new Checkbox("Speed vs Contig", rtGrp, true);
        speedvscontig.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Speed vs Contig selected");
                configtype = Config_SpeedvsContig;

            }
        });

        rtGrp.setSelectedCheckbox(jevsbc);

        f2.add(jevsbc);
        f2.add(jevscontig);
        f2.add(jevsspeed);
        f2.add(speedvscontig);
        runtypePanel.add(f2);
        return runtypePanel;
    }

    public JPanel runnamepanel() {
        JPanel algoPanel = new JPanel();
        JTextField f1 = new JTextField();
        f1.setText("Input name of run batch:");
        f1.setEditable(false);
        algoPanel.add(f1);
        JTextField f2 = new JTextField();
        f2.setPreferredSize(new Dimension(500, 20));
        f2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                batchRunName = f2.getText();
                System.out.println("Batch Run Name: " + batchRunName);

            }
        });

        algoPanel.add(f1);
        algoPanel.add(f2);
        return algoPanel;
    }
}