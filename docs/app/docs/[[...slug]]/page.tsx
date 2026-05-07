import { source } from '@/lib/source';
import {
  DocsPage,
  DocsBody,
  DocsTitle,
  DocsDescription,
} from 'fumadocs-ui/page';
import { notFound } from 'next/navigation';
import type { ComponentType } from 'react';

interface PageData {
  body: ComponentType;
  toc: unknown;
  title: string;
  description?: string;
}

interface Props {
  params: Promise<{ slug?: string[] }>;
}

export default async function DocPage({ params }: Props) {
  const { slug } = await params;
  const page = source.getPage(slug);
  if (!page) notFound();

  const data = page.data as unknown as PageData;
  const MDX = data.body;

  return (
    <DocsPage toc={data.toc as Parameters<typeof DocsPage>[0]['toc']}>
      <DocsTitle>{data.title}</DocsTitle>
      <DocsDescription>{data.description}</DocsDescription>
      <DocsBody>
        <MDX />
      </DocsBody>
    </DocsPage>
  );
}

export async function generateStaticParams() {
  return source.generateParams();
}

export async function generateMetadata({ params }: Props) {
  const { slug } = await params;
  const page = source.getPage(slug);
  if (!page) notFound();
  const data = page.data as unknown as PageData;
  return { title: data.title, description: data.description };
}
