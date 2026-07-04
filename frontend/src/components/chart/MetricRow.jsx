import React from 'react';

export const MetricRow = ({color, label, value}) => (
    <div style={{display: 'flex', alignItems: 'center', fontSize: 13, color: '#374151'}}>
        <span
            style={{
                display: 'inline-block',
                width: 12,
                height: 12,
                borderRadius: '50%',
                background: color,
                marginRight: 10,
            }}
        />
        <span style={{marginRight: 6}}>{label}:</span>
        <b style={{fontWeight: 700, color: '#111827'}}>{value}</b>
    </div>
);