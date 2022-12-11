# GmailOAUTH2Utilities

This is a collection of tools that help you generate access tokens and refresh tokens of Google / Gmail. It contains both command line tool and user-friendly UI tool. The command line tool does exactly the same thing as oauth2.py does, except it is written in Java and does not require you to install Python on your computer. Apart from all these, the UI tool also enables you to convert base64 string to normal text. Before using this application, you are required to create a project and OAuth Client ID at Google Cloud platform. When creating an OAuth Client ID, you will be asked to select an “Application type”. The Gmail OAuth2 Utilities supports Universal Windows Platform (UWP) and Web application. When it comes to Web application the Redirect URI must be consistent with what is specified in the Google Cloud platform. 

To learn more about this set of tools, please watch the following playlist of video tutorials. 


https://www.youtube.com/watch?v=IV3PN7IejTg&list=PLmq7EQU8fYwYGKWnsnKJgakZLYW1AC8Zd


Change logs: 

v1.1.0
1. Users are allowed to change Redirect URI real time (used to generate OAuth2 Access Token for the first time). 
2. Gmail OAuth2 Utilities starts a simple HTTP server, hosting a simple web page that displays the Verification Code redirected from Google and allows users to copy it. 
3. Support application type of Web Application (the one users may choose when creating an OAuth Client ID at Google Cloud Platform). 
