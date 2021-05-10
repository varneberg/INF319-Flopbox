# Flopbox

## Description:

We intend to create an application that aims to present developers with common mistakes made in application development with the intent to highlight these mistakes and what the consequences may be. By providing an interactive user experience, our goal is to give users of the application a better understanding of common implementation mistakes with the hope that they won't make the same mistakes when developing applications in the future.

The educational part of our project acts as an interactive layer that lies upon a purposefully client-server storage service where the client should be able to log in to the server through a database and store files that the server stores in its internal storage. The client should be able to obtain their stored files for later access. As a way to secure client privacy, both the communication channel between the client and the server, as well as the stored files on the server will be encrypted with either flawed cryptographic algorithms or poorly chosen cryptographic primitives. 

Structurally the program will be written insecurely in order to highlight and give practical examples of these insecurities to the end-user. Our chosen programming language is java structured with maven, a MySQL database for storing user credentials and, and a GUI to provide a better user experience.

