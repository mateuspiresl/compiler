program teste; {programa exemplo}
var
	valor1: integer;

procedure calcula_percentual (n1:real; per1:real);
var
	valor2: real;
begin 
    valor2 := n1 * (21/100) + per1; 
end;

begin
  calcula_percentual(1.0, 2.0);
	valor1 := 1;
  valor2 := 2.0;
end.