THIS NEEDS JAVA 21!!!!!!!
get it here!!!!: https://download.oracle.com/java/21/archive/jdk-21.0.3_windows-x64_bin.zip

to run, open cmd IN THIS DIRECTORY and type "path/to/java/21/bin/folder/java.exe -jar fnffd-je.jar"

you will probably see errors along the lines of "COULD NOT FIND OBJECT TO DRAW WITH DRAW ID (number)" but it doesnt matter,
as long as the game still runs and works its fine

default binds are dfjk, cant change these yet srry just deal

to change the song go into SONG_TO_LOAD_NAME.txt and change it to any file name in the charts folder WITHOUT THE SWOWS!!!
the current thing in there is a good example of what to do with it

yeah you can probably put in your own  songs and charts, heres a guide on how to do it
drop a .swows file into the charts folder named the same thing as the song
drop the song IN WAV FORMAT (VERY VERY IMPORTANT!!!) into the songs folder
ignore the .swews files they're useless as of rn

new as of 8/13/24:
you can add your own character!!

make a new folder in "/img/" and call it the name of your character (or just duplicate dude's)
get all your separate animation frames and pack them horizontally, i used https://www.codeandweb.com/free-sprite-sheet-packer (make sure to have 0 px padding!)
name it the animation (as seen in the dude folder)
make (or open and edit) a file called "frameData.txt" in the folder, this is where the game reads how many frames an animation has
write all the frame numbers with the format "animation:frame_number" for each of them, using the same animation name as the image files
go to the root folder and set the "false" in the "LOAD_OFFSET_EDITOR.txt" file to "true"
change the "DUDE_CHAR_TO_LOAD.txt" folder to be the character folder name and run the jar
get all the things lined up and hit enter, it should have copied all the data to your clipboard
make (or open and edit) a file called "offsets.txt" in the folder and paste in there
almost done! make (or open and edit) a file called "animData.txt" in the folder. this is where you tell the game if you have an "ayy", "miss" or "alt" set of anims
write "has-alts", "has-misses" and "has-ayy" on separate lines, then add a colon and a "true" or "false" after them depending on if the character has these animations
you're pretty much done! 