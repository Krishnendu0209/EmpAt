# EmpAt

An online employee attendance system where employees can check in an check out their respective attendance timings,
irrespective of the android device whether they do it from thier own device or from someone else's device.
Only employee code and password is all that is required for the employee to submit their attendance hence marking attendance at the tp of their fingertips.

## Online Database
* FireBase Realtime DataBase
## Online authentication
* FireBase Email Authentication


## Assumptions And Business Cases Considered

### Types of users :

* #### Prime Admin User -- This user detail is hard coded in the app so that the end user can set up other admin or other users according to their employee structure of their organisation for the first time. The user is not registered in the database. This user will be able to only register i.e create a new employee, the idea is simply to enable the enable the end user to create first admin user from their organisation
		User Id : ****.
		Password : ****.
* #### Admin User -- Will be created by end user with Admin rights
* #### Other User -- (admin and other) will be having access to submit attendance module where check in will be available if the user has not yet checked in that day. Similarly, if the user has already checked in for that calendar day then the user can check out only and no further check in allowed

  > Note : If the system finds that user has already checked out for that day then only the option to update checkout time will be given.

### Features / Modules accessible :

* #### Register User New User can be registered -- Only availabe to Admin User and the Prime Admin User
* #### Modify Employee Record -- Employee Details like name, email etc. can be modified or deleted -- Only availabe to an admin user.
* #### Modify Attendance Transactions -- Attendance transaction for a particular employee on a specific date can be modified or deleted. --  Only availabe to an admin user.
* #### Submit Attendance -- Where an user can submit their respective check in and check out times. -- Available to both other user and admin user.
* #### Get Attendance Report -- If user is Other user then he/she will be able to fetch only their own attandance report whereas if the user is of Admin type then he / she can get any other emloyee's attendance report, finally the report will be saved in the device.

