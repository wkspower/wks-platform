import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Adaptive Case Management',
    Svg: require('@site/static/img/chamaleon.svg').default,
    description: (
      <>
        Agile, flexible approach for unpredictable scenarios, driven by knowledge workers.
      </>
    ),
  },
  {
    title: 'Flexible Workflow Automation',
    Svg: require('@site/static/img/camunda-logo-dark.svg').default,
    description: (
      <>
        Empowering adaptive case management and process automation synergy integrated with Camunda.
      </>
    ),
  },
  {
    title: 'Open-source',
    Svg: require('@site/static/img/padlock.svg').default,
    description: (
      <>
        Collaboration, transparency, innovation, and cost-effectiveness, benefiting from a community-driven approach to continuous improvement.
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