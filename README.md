# GmailOAUTH2Utilities

This is a collection of tools that help you generate access tokens and refresh tokens of Google / Gmail. It contains both command line tool and user-friendly UI tool. The command line tool does exactly the same thing as oauth2.py does, except it is written in Java and does not require you to install Python on your computer. Apart from all these, the UI tool also enables you to convert base64 string to normal text. Before using this application, you are required to create a project and OAuth Client ID at Google Cloud platform. When creating an OAuth Client ID, you will be asked to select an “Application type”. The Gmail OAuth2 Utilities supports Universal Windows Platform (UWP) and Web application. When it comes to Web application the Redirect URI must be consistent with what is specified in the Google Cloud platform. 

## Installation Guide
Users do not have to run installers or change the registries of their operating systems. All they need to do is extract the zip file and start using the UI tool or the command line tool. Links to detailed video tutorials are provided below. To prevent all the individual files inside the zip file from being extracted into the current working directory, you may move the zip file into a sperate empty folder (directory) before unzipping it. 
Once you start using the Gmail OAuth2 Utilities, it creates a folder (or directory), named "log", under the same path as its own, if you are Windows or Ubuntu users. But if you are a Mac user, you will find a folder under your home directory, named "GmailOAUTH2Utilities". This folder contains saved input data under "config" sub-folder and log files under "log" sub-folder.

## Uninstallation Guide
To remove this app, all you need to do is manually delete the files extracted from the zip file you have downloaded. Apart from that, Mac users also need to manually delete "GmailOAUTH2Utilities" folder (and its content) under their home directories, to completely remove this app.
To learn more about this set of tools, please watch the following playlist of video tutorials. 


https://www.youtube.com/watch?v=IV3PN7IejTg&list=PLmq7EQU8fYwYGKWnsnKJgakZLYW1AC8Zd


Change logs: 

v1.1.0
1. Users are allowed to change Redirect URI real time (used to generate OAuth2 Access Token for the first time). 
2. Gmail OAuth2 Utilities starts a simple HTTP server, hosting a simple web page that displays the Verification Code redirected from Google and allows users to copy it. 
3. Support application type of Web Application (the one users may choose when creating an OAuth Client ID at Google Cloud Platform). 
