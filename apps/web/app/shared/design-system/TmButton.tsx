import { Button, type ButtonProps } from "@mui/material";

export function TmButton({ children, ...props }: ButtonProps) {
  return (
    <Button {...props} disableElevation>
      {children}
    </Button>
  );
}
