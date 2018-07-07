# Define a variable for classpath
CLASS_PATH = ../bin

C_FILES := $(wildcard luadec/*.c)
OBJ64_FILES := $(addprefix luadec/obj/,$(notdir $(C_FILES:.c=.o)))
OBJ_FILES := $(addprefix luadec/obj/,$(notdir $(C_FILES:.c=.o)))

# Define a virtual path for .class in the bin directory
vpath %.class $(CLASS_PATH)

all : luadec.dll

# $@ matches the target, $< matches the first dependancy
luadec.dll : $(OBJ_FILES)
	gcc -Wl,--add-stdcall-alias -shared -o $@ $^

# $@ matches the target, $< matches the first dependancy
luadec/obj/%.o: luadec/%.c java_luadec.h 
	gcc -m32 -I"C:\Program Files\Java\jdk1.6.0_43\include" -I"C:\Program Files\Java\jdk1.6.0_43\include\win32" -c $< -o $@

# $* matches the target filename without the extension
java_luadec.h : ca/fraggergames/ffxivextract/helpers/LuaDec.class
	javah -classpath $(CLASS_PATH) -o ./luadec/java_luadec.h ca.fraggergames.ffxivextract.helpers.LuaDec 

clean :
	rd luadec\obj