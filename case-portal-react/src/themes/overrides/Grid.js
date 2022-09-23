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
                    fontWeight: 'bold'
                },
                cell: {
                    borderColor: theme.palette.divider
                }
            }
        }
    };
}
