import { Box, Typography } from "@mui/material";
import type { KeyboardEvent, ReactNode } from "react";
import { tmTokens } from "~/shared/theme/tmTokens";

export interface TmDataTableColumn<T> {
  header: string;
  id: string;
  render: (row: T) => ReactNode;
  width: string;
}

interface TmDataTableProps<T> {
  columns: TmDataTableColumn<T>[];
  getRowId: (row: T) => string;
  onRowClick?: (row: T) => void;
  rowAriaLabel?: (row: T) => string;
  rows: T[];
}

export function TmDataTable<T>({
  columns,
  getRowId,
  onRowClick,
  rowAriaLabel,
  rows,
}: TmDataTableProps<T>) {
  const gridTemplateColumns = columns.map((column) => column.width).join(" ");

  const openRow = (row: T) => {
    if (onRowClick) {
      onRowClick(row);
    }
  };

  const onRowKeyDown = (event: KeyboardEvent<HTMLDivElement>, row: T) => {
    if (!onRowClick) {
      return;
    }
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openRow(row);
    }
  };

  return (
    <Box role="table" sx={{ overflowX: "auto" }}>
      <Box sx={{ minWidth: 760 }}>
        <Box
          role="row"
          sx={{
            backgroundColor: tmTokens.colors.surfaceLow,
            borderBottom: `1px solid ${tmTokens.colors.border}`,
            display: "grid",
            gap: 2,
            gridTemplateColumns,
            px: 3,
            py: 1.5,
          }}
        >
          {columns.map((column) => (
            <Typography
              component="div"
              key={column.id}
              role="columnheader"
              sx={{
                ...tmTokens.typography.labelSm,
                color: tmTokens.colors.secondary,
                textTransform: "uppercase",
              }}
            >
              {column.header}
            </Typography>
          ))}
        </Box>

        {rows.map((row, index) => (
          <Box
            aria-label={rowAriaLabel?.(row)}
            key={getRowId(row)}
            onClick={() => openRow(row)}
            onKeyDown={(event) => onRowKeyDown(event, row)}
            role="row"
            tabIndex={onRowClick ? 0 : undefined}
            sx={{
              alignItems: "center",
              backgroundColor: tmTokens.colors.surfaceLowest,
              borderBottom: index === rows.length - 1 ? "none" : `1px solid ${tmTokens.colors.border}`,
              cursor: onRowClick ? "pointer" : "default",
              display: "grid",
              gap: 2,
              gridTemplateColumns,
              minHeight: 64,
              px: 3,
              transition: "background-color 120ms ease",
              "&:focus-visible": {
                outline: `2px solid ${tmTokens.colors.primaryContainer}`,
                outlineOffset: -2,
              },
              "&:hover": {
                backgroundColor: tmTokens.colors.surfaceLow,
              },
            }}
          >
            {columns.map((column) => (
              <Box key={column.id} role="cell" sx={{ minWidth: 0 }}>
                {column.render(row)}
              </Box>
            ))}
          </Box>
        ))}
      </Box>
    </Box>
  );
}
