# Maze (Java FX)

The idea of the game is that a man from 2024 is stuck in different periods of time. He needs to navigate through each period and collect specific items to move to the next one. Once the player collects all items from all levels, he will return to his original time period. The player will move using the left, right, up, and down arrows. To complete the game, you must finish 3 levels. To transition between levels, you will use portals, which you can locate after collecting all items in a particular level.

- Level 1: 
  - Ancient Egypt: Maze-style grid with yellow and brown tones
  - Elements to collect: Hieroglyphs.
  - Monsters: Crazy Mommies following you.
- Level 2: 
  - Medieval Times: Maze-style grid with a yellow and green canvas, adorned with castles
  - Elements to collect: Gold.
  - Monsters: Crazy Wizards and Knights.
- Level 3:
  - Dysphoria Future: Illuminated maze with glowing green and pink colours, featuring tall dark buildings in the background
  - Elements to collect: Robot Heads.
  - Monster: Crazy Aliens following you.

https://github.com/user-attachments/assets/b05f861a-0d9b-4351-bf00-470073a5fa64

## Instructions to Run the Project

- Install Eclipse IDE
- Clone the Repository from GitHub
- Go to Eclipse IDE
- File > Import > Git > Projects from Git.
- Choose Clone URI and click Next.
- Paste the URL of the GitHub repository into the URI field and press Next.
- Select a local directory to clone the project to, and click Next.
- Choose Import as Maven Project then click Finish.
- Download Java (I use Java 22.0.2)
- Download the JavaFX SDK (I use javafx-sdk-21.0.4)
- Add JavaFX Libraries to the Project in Eclipse
- Right-click the imported project in Eclipse
- Choose Properties.
- Java Build Path > Libraries tab.
- Click Add External JARs and navigate to the lib folder of the extracted JavaFX SDK.
- Select all the JAR files in the lib folder and click Open.
- Click Apply and Close.
- Configure JavaFX in the Run Configuration
- Go to Run > Run Configurations
- Select the run configuration for your JavaFX project (Menu.java is the main class).
- In the Arguments tab, in the VM arguments section, add the following:
  ```
  --module-path /path-to-javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
  ```
- Apply the changes and close the configuration window.


