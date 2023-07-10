import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Adaptive Case Management',
    Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
        WKS Platform provides a powerful Adaptive Case Management (ACM) solution built on top of Camunda BPM.
      </>
    ),
  },
  {
    title: 'Flexible Workflow Automation',
    Svg: require('@site/static/img/camunda-logo-dark.svg').default,
    description: (
      <>
        Automate complex workflows and adapt them on-the-fly with WKS Platform's flexible automation capabilities.
      </>
    ),
  },
  {
    title: 'Extensible and Customizable',
    Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
        Extend and customize WKS Platform to meet your specific case management requirements.
      </>
    ),
  },
];

function Feature({ Svg, title, description }) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}