import React from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';

const dependencies = {
    jdk: '17',
    maven: '3.8.+',
    node: 'v16.16+'
};

export function AppName() {
    const { siteConfig } = useDocusaurusContext();
    const { title } = siteConfig;

    return <span style={{ 'fontWeight': 'bold' }}>{title}</span>;
}

export function JdkVersion() {
    return dependencies.jdk;
}

export function MavenVersion() {
    return dependencies.maven;
}

export function NodeVersion() {
    return dependencies.node;
}