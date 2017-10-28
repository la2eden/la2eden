# Installation guide

**Dependencies:**
  - Compiled source files _(La2Eden.zip)_
  - [MySQL Server](https://dev.mysql.com/downloads/mysql/5.6.html)
  - **[OPTIONAL]** [HeidiSQL](http://www.heidisql.com/download.php) for database management

**Steps:**
1. Download and install MySQL from the list above
2. Extract the `La2Eden.zip` file
3. Go to `installer` folder
4. Install the database tables
  - Use `Database_Installer_GS.jar` to install the **gameserver** database.
  - Use `Database_Installer_LS.jar` to install the **loginserver** database.
5. Go to **loginserver** folder
  - Run `RegisterGameserver.bat`
  - Put the generated `hexid.txt` inside the `gameserver/config` folder
  - Run `startLoginServer.bat`
6. Go to **gameserver** folder
  - Run `startGameServer.bat`
7. Delete the `installer` folder

**And you're good to go, mate!**