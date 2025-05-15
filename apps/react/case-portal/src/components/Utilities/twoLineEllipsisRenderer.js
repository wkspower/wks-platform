export const renderTwoLineEllipsis = (params) => (
  <div
    title={params.value}
    style={{
      display: '-webkit-box',
      WebkitLineClamp: 2,
      WebkitBoxOrient: 'vertical',
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      whiteSpace: 'normal',
      wordBreak: 'break-word',
      lineHeight: '1.4em',
      maxHeight: '2.8em',
    }}
  >
    {params.value}
  </div>
)
