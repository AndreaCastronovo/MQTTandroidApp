# My bachelor's thesis of Electronic Engineering at University of Bologna
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the NuriCheat and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Thanks again! Now go create something AMAZING! :D
***
***
***
*** To avoid retyping too much info. Do a search and replace for the following:
*** Stikinit, kf-eval, twitter_handle, email, project_title, project_description
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![GitHub followers][github-shield]][github-url]


<!-- PROJECT LOGO -->
<p align="center">
  <img src="https://github.com/AndreaCastronovo/MQTTandroidApp/blob/main/Screenshot_20220519-215955_One%20UI%20Home.jpg" alt="logo" height="100" width="90"
</p>   
  
<br />
<p align="center">
  <a href="https://github.com/Federicoand98/AR-Dice">
  </a>
  <h1 align="center">ARDice</h1>
  <p align="center">
    ARDice is an augmented reality application that allows you to roll dice. 
    <br /> 
    <br />
    <a href="https://github.com/Federicoand98/AR-Dice/blob/main/ARDice_ARelazione.pdf">In-Depth Document (Italian)</a>
    ·
    <a href="https://github.com/Federicoand98/AR-Dice/issues">Report Bug</a>
    ·
    <a href="https://github.com/Federicoand98/AR-Dice/issues">Request Feature</a>
  </p>
</p>



<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project
**ARDice** is an **augmented reality application** that allows you to roll dice.

### Game Mode:
This is the default mode in which the application is started up, in which it is possible to roll the dice very simply. In this mode, the Augmented Reality engine continuously detects surfaces and walls, therefore vertical and horizontal planes, making them interact with the objects in play.
In Game Mode there are 2 launch modes:
  1. **Swipe to Throw**: the die is thrown by swiping it, and then the die completes a parabola that starts from the lower part of the smartphone display and arrives in the AR environment
  2. **Falling Die**: in this mode there will be a pointer on the plane, whose position is given by an estimate of the depth. Pressing the large button at the bottom center will make it fall vertically in proximity to the pointer.

To change launch mode you need to use the button at the top center.
It is also possible to change the dice to be thrown using the appropriate buttons positioned at the bottom, on the sides of the launch key.

<table style="border: none">
  <tr>
    <td>Swipe to throw mode</td>
    <td>Falling Dice mode</td>
  </tr>
  <tr>
    <td width="20%"><img src="imgs/swipe.gif"/></td>
    <td width="20%"><img src="imgs/falling.gif"/></td>
  </tr>
</table>

### Table Mode:
The Table Mode is an advanced mode that allows you to build your own horizontal plane at will.
This mode is accessible by pressing the key at the top right.
As soon as TableMode is enabled, all automatically constructed plans will be destroyed.
In this way the user, through a pointer positioned based on an estimate of depth and surfaces,
He can select the vertices of the surface he wants to build, and once this is done, clicking the button at the bottom center will finalize the currently constructed floor by creating walls around the perimeter.
It is also possible to move a vertex by simply clicking on it and then it is possible to confirm it by pressing the add vertex button.
  - **Build:** button to build walls, clickable only if there are at least 3 points
  - **+:** key to add a vertex
  - **-:** key to remove a vertex, double tap to confirm
  - **Trash:** key to delete the newly built surface
  - **Save:** button to save the newly built floor and return to game mode, if the walls have not been built it is not clickable

<p align="center">
<img src="imgs/table.gif" align="center"/>
</p>

### Presets:
You can change and save your presets to be used in falling dice mode.

### Themes:
This settings page can let you modify the colors of your dice. Tap the number to swich from the number to the die color.

### Built With

* [Visual Studio Code](https://code.visualstudio.com/download)
* [Unity](https://unity.com/download)

<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites
* Git
* Visual Studio
* Unity

### Installation

1. Install Git at [Download Git](https://git-scm.com/download).
2. Clone the repo.
   ```sh
   git clone https://github.com/Federicoand98/AR-Dice
   ```
4. Install the latest version of Unity.
5. Open the directory with Visual Studio.
6. Unzip the 7z file in the Resources folder.



<!-- USAGE EXAMPLES -->
## Usage

To use the application:
* Switch Unity build platform (iOS, Android)
* Build and Run



<!-- ROADMAP -->
## Roadmap

See the [open issues](https://github.com/Federicoand98/AR-Dice/issues) for a list of proposed features (and known issues).

### Future developments


<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request



<!-- LICENSE -->
## License

Distributed under the MIT License. See [`LICENSE`](https://github.com/Federicoand98/AR-Dice/blob/main/LICENSE) for more information.



<!-- CONTACT -->
## Contact

Federico Andrucci - federico.andrucci@gmail.com <br>
Alex Gianelli - djgiane@yahoo.it <br>
Lorenzo Righi - righi.lo@gmail.com<br>

Project Link: [https://github.com/Federicoand98/AR-Dice](https://github.com/Federicoand98/AR-Dice)






<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/AndreaCastronovo/MQTTandroidApp.svg?style=for-the-badge
[contributors-url]: https://github.com/AndreaCastronovo/MQTTandroidApp/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/AndreaCastronovo/MQTTandroidApp.svg?style=for-the-badge
[forks-url]: https://github.com/AndreaCastronovo/MQTTandroidApp/network/members
[stars-shield]: https://img.shields.io/github/stars/AndreaCastronovo/MQTTandroidApp.svg?style=for-the-badge
[stars-url]: https://github.com/AndreaCastronovo/MQTTandroidApp/stargazers
[issues-shield]: https://img.shields.io/github/issues/AndreaCastronovo/MQTTandroidApp.svg?style=for-the-badge
[issues-url]: https://github.com/AndreaCastronovo/MQTTandroidApp/issues
[license-shield]: https://img.shields.io/github/license/AndreaCastronovo/MQTTandroidApp.svg?style=for-the-badge
[license-url]: https://github.com/AndreaCastronovo/MQTTandroidApp/blob/ardice-main/LICENSE
[github-shield]: https://img.shields.io/github/followers/AndreaCastronovo.svg?style=social&label=Follow
[github-url]: https://github.com/AndreaCastronovo
