import { Suspense } from 'react';
import Loader from './Loader';

function Loadable(Component) {
  return function LoadableComponent(props) {
    return (
      <Suspense fallback={<Loader />}>
        <Component {...props} />
      </Suspense>
    );
  };
}

export default Loadable;
