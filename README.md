# JavaFX desktop online chat

## Task 
Develop a program to communicate with multiple users on a network created on java sockets. 
Be able to create private and group dialogue.

## Use cases
1) Connect/Disconnect from server
2) Send message to selected chat
3) Create private chat with online user
4) Invite users to chat

## Some explanations
1) All user actions do not take place in the application, but send an appropriate request to the server and receive a response, the processing of which is provided in advance.
2) When a user connects to the server, he is added to the list of connected users, which is sent to all clients. Shutdown works the same way.
3) There is a main group chat "Group chat" which cannot be deleted. All connected users are added to it.
4) Communication between the user and the server takes place using serializable DTO objects.
5) To simplify the creation of DTO objects, a third-party lombok library was used.

## Preview images
<img width="912" alt="Screenshot 2023-05-22 at 19 18 54" src="https://github.com/radonious/term-4-course-work/assets/67727902/9b262f53-d123-467a-8311-664c8d6190c5">
<img width="362" alt="Screenshot 2023-05-22 at 19 20 25" src="https://github.com/radonious/term-4-course-work/assets/67727902/89953656-d3ab-4d35-8c2c-7bfdca8293fe">

#### (c) radon
