program lights;
var a,b:integer;
procedure textbackground;
begin end;
procedure random;
begin end;
begin
textbackground(white);
a:=random(4)+1;
if a=1 then textbackground(blue);
if a=2 then textbackground(red);
if a=3 then textbackground(green);
if a=4 then textbackground(yellow);
end.
