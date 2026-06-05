import { Tab, Tabs } from "@mui/material";
import { tmTokens } from "~/shared/theme/tmTokens";

interface TmFilterTabItem<T extends string> {
  label: string;
  value: T;
}

interface TmFilterTabsProps<T extends string> {
  items: TmFilterTabItem<T>[];
  onChange: (value: T) => void;
  value: T;
}

export function TmFilterTabs<T extends string>({ items, onChange, value }: TmFilterTabsProps<T>) {
  return (
    <Tabs
      onChange={(_, nextValue: T) => onChange(nextValue)}
      value={value}
      sx={{
        borderBottom: `1px solid ${tmTokens.colors.border}`,
        minHeight: 36,
      }}
    >
      {items.map((item) => (
        <Tab key={item.value} label={item.label} value={item.value} />
      ))}
    </Tabs>
  );
}
