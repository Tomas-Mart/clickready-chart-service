import React from 'react';
import {COLORS} from './config/colors';
import {MetricRow} from './MetricRow';

export const CustomTooltip = ({active, payload}) => {
    if (active && payload && payload.length) {
        const pointData = payload[0]?.payload;
        if (!pointData || pointData.isMidPoint) return null;

        const {rawDate, cost, cpa, roi, conversions} = pointData;

        return (
            <div
                style={{
                    background: 'white',
                    padding: '12px 16px',
                    borderRadius: 8,
                    boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
                    border: '1px solid #e5e7eb',
                    minWidth: 190,
                    fontFamily: 'system-ui, sans-serif',
                }}
            >
                <div style={{fontWeight: 600, fontSize: 14, color: '#111827', marginBottom: 8}}>
                    {rawDate || ''}
                </div>
                <div style={{display: 'flex', flexDirection: 'column', gap: 4}}>
                    {cost !== undefined && cost !== null && (
                        <MetricRow color={COLORS.yellowFade} label="Cost" value={cost.toFixed(2)}/>
                    )}
                    {cpa !== undefined && cpa !== null && (
                        <MetricRow color={COLORS.blue} label="CPA" value={cpa.toFixed(2)}/>
                    )}
                    {roi !== undefined && roi !== null && (
                        <MetricRow color={COLORS.green} label="ROI confirmed" value={roi.toFixed(2)}/>
                    )}
                    {conversions !== undefined && conversions !== null && (
                        <MetricRow color={COLORS.purple} label="Conversions" value={conversions}/>
                    )}
                </div>
            </div>
        );
    }
    return null;
};
