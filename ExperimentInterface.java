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
	
	private static String Default_Output_Location = "C:/Users/Ollie/Documents/MSc Studying/Project/Output Data/";

	private IllumConfig config = new IllumConfig(0,0,0,0,(Path)null,"");

    //Instantiate objects we'll need
    JFrame mainFrame;
    JPanel mainPanel;

    //Set destination of output folders here
    private static File Output_Location;

    private int defaultoffspring = 20000;

    //Default values
    private Integer algotype;
    private Integer numberofruns = 1;
    private Integer numberoffspring = defaultoffspring;
    private Integer config_param1;
    private Integer config_param2;
    private String batchRunName;
    
    private float minutesPerLevel = 0.035f;

    public static void main(String[] args) {

        ExperimentInterface ei = new ExperimentInterface();

        ei.run();
    }

    public void run() {

        mainFrame = new JFrame();
        mainFrame.setTitle("Run Initiator"); 
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
                if (formCompleteValidation()) {
	            	System.out.println("Form submitted. Initialising Experiments");
	                System.out.println("Algorithm type: " + algotype + ". Num Runs: " + numberofruns + " param1: " + config_param1 + " and param2: " + config_param2 );
	
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
	                                  
	                    config = new IllumConfig(algotype, numberoffspring, config_param1, config_param2, runPath, runName);
	
	                    ExperimentRun currRun = new ExperimentRun(config);
	                    currRun.run();
	                    currRun = null;
	                }
	
	                System.exit(0);
                }
                else {
                	System.out.println("Please complete form before submitting");
                }

            }
        });

        mainPanel.add(submitButton);

        //Adding panel to frame
        mainFrame.add(mainPanel);

        mainFrame.setSize(500, 1000);
        mainFrame.setVisible(true);

    }

    public JPanel outLocPanel() {
        JPanel outLocPanel = new JPanel();
        GridLayout layout = new GridLayout(0,2);
        outLocPanel.setLayout(layout);
        JTextField panelText = new JTextField();
        panelText.setText("Choose output folder");
        panelText.setEditable(false);
        JButton selectB = new JButton();
        selectB.setText("Please Select");
        selectB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File(Default_Output_Location));
                chooser.setDialogTitle("Select Output Folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //
                // disable the "All files" option.
                //
                chooser.setAcceptAllFileFilterUsed(false);
                //    

                if (chooser.showOpenDialog(selectB) == JFileChooser.APPROVE_OPTION) {

                    System.out.println("Selected Folder : " +
                        chooser.getSelectedFile());

                    Output_Location = chooser.getSelectedFile();
                    selectB.setText("Current folder: " + Output_Location.getName());
                } else {
                    System.out.println("No Selection ");
                }


            }
        });
        
        outLocPanel.add(panelText);
        outLocPanel.add(selectB);
        

        return outLocPanel;

    }



    public JPanel algoPanel() {
    	//Overall panel
        JPanel algoPanelWrap = new JPanel();     
        GridLayout twoCol = new GridLayout(0,2);
        algoPanelWrap.setLayout(twoCol);
        //Left hand text sub-panel
        JTextField panelText = new JTextField();
        panelText.setText("Select algorithm:");
        panelText.setEditable(false);
        algoPanelWrap.add(panelText);
        //Right hand algorithm options panel
        JPanel panelAlgoOpts = new JPanel();
        GridLayout oneCol = new GridLayout(0,1);
        panelAlgoOpts.setLayout(oneCol);
        
        CheckboxGroup algoGrp = new CheckboxGroup();
        Checkbox me = new Checkbox("MAP-Elites", algoGrp, true);
        me.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Map Elites Selected");
                algotype = config.Algo_MapElites;

            }
        });

        Checkbox shinecd = new Checkbox("SHINE-CD", algoGrp, true);
        shinecd.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Shine-CD selected");
                algotype = config.Algo_ShineCD;

            }
        });
        
        Checkbox shinefit = new Checkbox("SHINE-FIT", algoGrp, true);
        shinefit.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Shine-Fit selected");
                algotype = config.Algo_ShineFit;

            }
        });
        
        Checkbox shineHybrid = new Checkbox("SHINE-CD (Fit/Shine Hybrid)", algoGrp, true);
        shineHybrid.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Shine-Fit/Shine Hybrid selected");
                algotype = config.Algo_ShineHybrid;

            }
        });
        //Set default
        algoGrp.setSelectedCheckbox(me);
        algotype = config.Algo_MapElites;
        
        panelAlgoOpts.add(me);
        panelAlgoOpts.add(shinecd);
        panelAlgoOpts.add(shinefit);
        panelAlgoOpts.add(shineHybrid);
        algoPanelWrap.add(panelAlgoOpts);
        
        return algoPanelWrap;
    }

    
    public JPanel numrunPanel() {
        JPanel numrunPanel = new JPanel();
        numrunPanel.setPreferredSize(new Dimension(400, 200));
        JTextField textPanel = new JTextField();
        GridLayout twoCol = new GridLayout(0,2);
        numrunPanel.setLayout(twoCol);
        textPanel.setText("Number of full runs: ");
        textPanel.setEditable(false);
        numrunPanel.add(textPanel);
        JPanel selectPanel = new JPanel();


        Choice runOpts = new Choice();
        for (int i = 0; i < 5; i++) {
            runOpts.add(String.valueOf(i + 1));
        }

        runOpts.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                numberofruns = Integer.parseInt(runOpts.getSelectedItem());
            }
        });


        selectPanel.add(runOpts);
        numrunPanel.add(selectPanel);
        return numrunPanel;


    }

    public JPanel numoffspringPanel() {
        JPanel numoffspringPanelWrapper = new JPanel();
        GridLayout oneCol = new GridLayout(0,1);
        numoffspringPanelWrapper.setLayout(oneCol);
        JTextField topTextPanel = new JTextField();
        topTextPanel.setText("Number of offspring per run: ");
        topTextPanel.setEditable(false);
        numoffspringPanelWrapper.add(topTextPanel);

        //Storage for slider
        JPanel sliderPanel = new JPanel();
        //Storage for current slider value
        JSlider offspringSlider = new JSlider(JSlider.HORIZONTAL, 0, 50000, defaultoffspring);
        offspringSlider.setPreferredSize(new Dimension(450, 50));
        offspringSlider.setMajorTickSpacing(10000);
        offspringSlider.setMinorTickSpacing(5000);
        offspringSlider.setPaintTicks(true);
        offspringSlider.setSnapToTicks(true);
        offspringSlider.setSize(numoffspringPanelWrapper.getSize());
        JTextArea offspringDetailsPanel = new JTextArea(3, 30);
        
        numberoffspring = offspringSlider.getValue();
        String s = "Offspring Count: " + Integer.toString(offspringSlider.getValue());
        if (algotype!=null) {
	        if (algotype == 1) {
	        	s+=("\n" + "MAP Elites Iterations: " + Integer.toString(offspringSlider.getValue()/2));
	        }
	        else if (algotype==2||algotype==3){
	        	s+=("\n" + "SHINE Gens (Size: " + config.Generation_Size + "): " + Integer.toString(offspringSlider.getValue()/config.Generation_Size));
	        }
        }

        s+=("\n" + "Estimated runtime: " + Double.toString(offspringSlider.getValue()*minutesPerLevel) + " minutes. Hours: " + String.format("%.2f", (offspringSlider.getValue()*minutesPerLevel)/60));
        offspringDetailsPanel.setText(s);

        offspringSlider.addChangeListener(new ChangeListener() {
            @Override
            
            public void stateChanged(ChangeEvent arg0) {
                numberoffspring = offspringSlider.getValue();
                String s = "Offspring Count: " + Integer.toString(offspringSlider.getValue());
                if (algotype == config.Algo_MapElites) {
                	s+=("\n" + "MAP Elites Iterations: " + Integer.toString(offspringSlider.getValue()/2));
                }
                else if (algotype == config.Algo_ShineCD){
                	s+=("\n" + "SHINE Gens (Size: " + config.Generation_Size + "): " + Integer.toString(offspringSlider.getValue()/config.Generation_Size));
                }
                s+=("\n" + "Estimated runtime: " + Double.toString(offspringSlider.getValue()*minutesPerLevel) + " minutes. Hours: " + String.format("%.2f", (offspringSlider.getValue()*minutesPerLevel)/60));
                offspringDetailsPanel.setText(s);
            }
        });
        

        sliderPanel.add(offspringSlider);
        numoffspringPanelWrapper.add(sliderPanel);

        offspringDetailsPanel.setEditable(false);
        numoffspringPanelWrapper.add(offspringDetailsPanel);

        return numoffspringPanelWrapper;


    }

    public JPanel runtypePanel() {
        JPanel runtypePanel = new JPanel();
        JTextField f1 = new JTextField();
        f1.setText("Run Configuration:");
        f1.setEditable(false);
        runtypePanel.add(f1);
        JPanel f2 = new JPanel();     
        
        //Initialise selection option for parameter 1
        JPanel f2_param1 = new JPanel();
        GridLayout oneCol = new GridLayout(0,1);
        f2_param1.setLayout(oneCol);
        CheckboxGroup param1_rtGrp = new CheckboxGroup();
        Checkbox param1_je = new Checkbox("Jump Entropy", param1_rtGrp, true);
        param1_je.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Jump Entropy Param 1 selected");
                config_param1 = config.config_paramJE;

            }
        });

        Checkbox param1_speed = new Checkbox("Speed", param1_rtGrp, true);
        param1_speed.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Speed Param 1 selected");
                config_param1 = config.config_paramSpeed;

            }
        });

        Checkbox param1_contig = new Checkbox("Contiguity", param1_rtGrp, true);
        param1_contig.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Contiguity Param 1 selected");
                config_param1 = config.config_paramContig;

            }
        });
        
        Checkbox param1_clearrows = new Checkbox("Clear Rows", param1_rtGrp, true);
        param1_clearrows.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Clear Rows Param 1 selected");
                config_param1 = config.config_paramClearRows;

            }
        });

        Checkbox param1_bc = new Checkbox("Block Count", param1_rtGrp, true);
        param1_bc.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Block count Param 1 selected");
                config_param1 = config.config_paramBC;

            }
        });

        Checkbox param1_smooth = new Checkbox("Aggregate Smoothness", param1_rtGrp, true);
        param1_smooth.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Aggregate Smoothness Param 1 selected");
                config_param1 = config.config_paramAgrSmooth;

            }
        });
        Checkbox param1_contigOverBC = new Checkbox("Contiguity/BC", param1_rtGrp, true);
        param1_contigOverBC.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Contiguity/BC Param 1 selected");
                config_param1 = config.config_paramContigOverBC;

            }
        });
        
        param1_rtGrp.setSelectedCheckbox(param1_je);
        config_param1 = config.config_paramJE;
        
        f2_param1.add(param1_je);
        f2_param1.add(param1_contig);
        f2_param1.add(param1_speed);
        //f2_param1.add(param1_clearrows);
        f2_param1.add(param1_bc);
        f2_param1.add(param1_smooth);
        f2_param1.add(param1_contigOverBC);
        f2.add(f2_param1);
        
        //Initialise selection option for parameter 2
        JPanel f2_param2 = new JPanel();
        f2_param2.setLayout(oneCol);
        CheckboxGroup param2_rtGrp = new CheckboxGroup();
        Checkbox param2_je = new Checkbox("Jump Entropy", param2_rtGrp, true);
        param2_je.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Jump Entropy Param 2 selected");
                config_param2 = config.config_paramJE;

            }
        });

        Checkbox param2_speed = new Checkbox("Speed", param2_rtGrp, true);
        param2_speed.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Speed Param 2 selected");
                config_param2 = config.config_paramSpeed;

            }
        });

        Checkbox param2_contig = new Checkbox("Contiguity", param2_rtGrp, true);
        param2_contig.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Contiguity Param 2 selected");
                config_param2 = config.config_paramContig;

            }
        });
        
        Checkbox param2_clearrows = new Checkbox("Clear Rows", param2_rtGrp, true);
        param2_clearrows.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Clear Rows Param 2 selected");
                config_param2 = config.config_paramClearRows;

            }
        });
        
        Checkbox param2_bc = new Checkbox("Block Count", param2_rtGrp, true);
        param2_bc.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Block count Param 2 selected");
                config_param2 = config.config_paramBC;

            }
        });
        
        Checkbox param2_smooth = new Checkbox("Aggregate Smoothness", param2_rtGrp, true);
        param2_smooth.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Aggregate Smoothness Param 1 selected");
                config_param2 = config.config_paramAgrSmooth;

            }
        });
        Checkbox param2_contigOverBC = new Checkbox("Contiguity/BC", param2_rtGrp, true);
        param2_contigOverBC.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Contiguity/BC Param 2 selected");
                config_param2 = config.config_paramContigOverBC;

            }
        });

        param2_rtGrp.setSelectedCheckbox(param2_je);
        config_param2 = config.config_paramJE;
        
        f2_param2.add(param2_je);
        f2_param2.add(param2_contig);
        f2_param2.add(param2_speed);
        //f2_param2.add(param2_clearrows);
        f2_param2.add(param2_bc);
        f2_param2.add(param2_smooth);
        f2_param2.add(param2_contigOverBC);
           
        f2.add(f2_param2);
        
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
    
    public boolean formCompleteValidation() {
    	if (Output_Location!=null&&algotype!=null&&config_param1!=null&&config_param2!=null) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
}