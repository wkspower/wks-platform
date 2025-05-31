import DeleteIcon from '@mui/icons-material/DeleteOutlined'

const DeleteCell = ({ dataItem, onDelete }) => (
  <td style={{ textAlign: 'center' }}>
    <DeleteIcon
      style={{ cursor: 'pointer' }}
      onClick={() => onDelete(dataItem.id)}
    />
  </td>
)

export default DeleteCell
