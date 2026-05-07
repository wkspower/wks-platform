import { docs } from '@/.source';
import { createMDXSource } from 'fumadocs-mdx';
import { loader } from 'fumadocs-core/source';

// fumadocs-mdx 11 ships files as a lazy function; fumadocs-core 15 loader expects
// the resolved array. Cast to any to bridge the type mismatch at the package boundary.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const mdxSource = createMDXSource(docs.docs, docs.meta) as any;

export const source = loader({
  baseUrl: '/docs',
  source: {
    ...mdxSource,
    files: typeof mdxSource.files === 'function' ? mdxSource.files() : mdxSource.files,
  },
});
