// ==============================|| OVERRIDES - TABLE CELL ||============================== //

export default function Grid(theme) {
    return {
        MuiDataGrid: {
            styleOverrides: {
                main: {
                    fontSize: '0.875rem'
                },
                columnHeaders: {
                    borderColor: theme.palette.divider
                },
                columnHeaderTitle: {
                    fontWeight: '500',
                },
                cell: {
                    borderColor: theme.palette.divider,
                    fontSize: '0.9rem',
                    color: theme.palette.secondary.main,
                }
            }
        }
    };
}
