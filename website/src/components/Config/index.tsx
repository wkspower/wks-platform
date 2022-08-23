import React from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';

export function AppName() {
    const {siteConfig} = useDocusaurusContext();
    const {title} = siteConfig;
    
    return <span style={{'fontWeight': 'bold'}}>{title}</span>;
}