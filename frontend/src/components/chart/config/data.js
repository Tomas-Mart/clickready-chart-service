import { SCALES } from './scales';

export const BASE_POINTS = [
    {date: '10.06.2026', cost: 2.04, cpa: 0.68, roi: 610.78, conversions: 3},
    {date: '11.06.2026', cost: 25.85, cpa: 0.86, roi: 180.5, conversions: 30},
    {date: '12.06.2026', cost: 44.36, cpa: 1.23, roi: 161.47, conversions: 36},
    {date: '13.06.2026', cost: 55.65, cpa: 0.79, roi: 56.33, conversions: 70},
    {date: '14.06.2026', cost: 63.75, cpa: 0.71, roi: 357.25, conversions: 90},
];

export const generateChartData = () => {
    const result = [];

    BASE_POINTS.forEach((point, index) => {
        result.push({
            date: point.date,
            rawDate: point.date,
            cost: point.cost,
            costScaled: point.cost * SCALES.cost,
            cpa: point.cpa,
            cpaScaled: point.cpa * SCALES.cpa,
            roi: point.roi,
            rooScaled: point.roi * SCALES.roi,
            conversions: point.conversions,
            conversionsScaled: point.conversions * SCALES.conversions,
            isMidPoint: false,
            x: index,
        });

        if (index < BASE_POINTS.length - 1) {
            const next = BASE_POINTS[index + 1];
            const t1 = 1 / 3;
            const midCost1 = point.cost + (next.cost - point.cost) * t1;
            const midCpa1 = point.cpa + (next.cpa - point.cpa) * t1;
            const midRoi1 = point.roi + (next.roi - point.roi) * t1;
            const midConversions1 = point.conversions + (next.conversions - point.conversions) * t1;

            result.push({
                date: `${point.date}_mid1`,
                rawDate: point.date,
                cost: midCost1,
                costScaled: midCost1 * SCALES.cost,
                cpa: midCpa1,
                cpaScaled: midCpa1 * SCALES.cpa,
                roi: midRoi1,
                rooScaled: midRoi1 * SCALES.roi,
                conversions: midConversions1,
                conversionsScaled: midConversions1 * SCALES.conversions,
                isMidPoint: true,
                x: index + t1,
            });

            const t2 = 2 / 3;
            const midCost2 = point.cost + (next.cost - point.cost) * t2;
            const midCpa2 = point.cpa + (next.cpa - point.cpa) * t2;
            const midRoi2 = point.roi + (next.roi - point.roi) * t2;
            const midConversions2 = point.conversions + (next.conversions - point.conversions) * t2;

            result.push({
                date: `${point.date}_mid2`,
                rawDate: point.date,
                cost: midCost2,
                costScaled: midCost2 * SCALES.cost,
                cpa: midCpa2,
                cpaScaled: midCpa2 * SCALES.cpa,
                roi: midRoi2,
                rooScaled: midRoi2 * SCALES.roi,
                conversions: midConversions2,
                conversionsScaled: midConversions2 * SCALES.conversions,
                isMidPoint: true,
                x: index + t2,
            });
        }
    });

    return result;
};