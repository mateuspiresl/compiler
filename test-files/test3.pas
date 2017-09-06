program lights;
uses crt;
var a,b:integer;
label 1;
begin
textbackground(white);
clrscr;
for a:=1 to 1000000 do inc b;
randomize;
1:
a:=random(4)+1;
if a=1 then textbackground(blue);
if a=2 then textbackground(red);
if a=3 then textbackground(green);
if a=4 then textbackground(yellow);
goto 1;
end.