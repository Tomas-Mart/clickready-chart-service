import React from 'react';
import {Area, CartesianGrid, ComposedChart, Line, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts';

// ✅ ТОЧНЫЕ ЦВЕТА для графика
const COLORS = {
    purple: '#A855F7',    // Фиолетовая линия (Conversions)
    green: '#22C55E',     // Зеленая плавная линия (ROI)
    blue: '#3B82F6',      // Синие точки (CPA)
    bgArea: '#FDE047',    // Желтая заливка (Area)
};

// ✅ Кастомный тултип с правильными типами
const CustomTooltip = ({active, payload, label}: any) => {
    if (active && payload && payload.length) {
        const cost = payload.find((p: any) => p.dataKey === 'cost')?.value;
        const cpa = payload.find((p: any) => p.dataKey === 'cpa')?.value;
        const roi = payload.find((p: any) => p.dataKey === 'roi')?.value;
        const conversions = payload.find((p: any) => p.dataKey === 'conversions')?.value;

        return (
            <div className="bg-white p-4 shadow-xl rounded-xl border border-gray-100 min-w-[160px]">
                <div className="text-gray-800 font-semibold text-base mb-2 border-b border-gray-100 pb-2">
                    {label}
                </div>
                {cost !== undefined && (
                    <div className="flex items-center justify-between gap-6 text-sm">
                        <span className="flex items-center gap-2 text-gray-600">
                            <span className="w-3 h-3 rounded-full bg-yellow-400 inline-block"/>
                            Cost:
                        </span>
                        <b className="text-gray-800">{cost.toFixed(2)}</b>
                    </div>
                )}
                {cpa !== undefined && (
                    <div className="flex items-center justify-between gap-6 text-sm">
                        <span className="flex items-center gap-2 text-gray-600">
                            <span className="w-3 h-3 rounded-full bg-blue-500 inline-block"/>
                            CPA:
                        </span>
                        <b className="text-gray-800">{cpa.toFixed(2)}</b>
                    </div>
                )}
                {roi !== undefined && (
                    <div className="flex items-center justify-between gap-6 text-sm">
                        <span className="flex items-center gap-2 text-gray-600">
                            <span className="w-3 h-3 rounded-full bg-green-500 inline-block"/>
                            ROI confirmed:
                        </span>
                        <b className="text-gray-800">{roi.toFixed(2)}</b>
                    </div>
                )}
                {conversions !== undefined && (
                    <div className="flex items-center justify-between gap-6 text-sm">
                        <span className="flex items-center gap-2 text-gray-600">
                            <span className="w-3 h-3 rounded-full bg-purple-500 inline-block"/>
                            Conversions:
                        </span>
                        <b className="text-gray-800">{conversions}</b>
                    </div>
                )}
            </div>
        );
    }
    return null;
};

// ✅ Данные по умолчанию
const DEFAULT_DATA = [
    {date: '10.06', cost: 0, cpa: 0, roi: 100, conversions: 0},
    {date: '11.06', cost: 20, cpa: 0.5, roi: 45, conversions: 10},
    {date: '12.06', cost: 44.36, cpa: 1.23, roi: 161.47, conversions: 36},
    {date: '13.06', cost: 55.65, cpa: 0.79, roi: 56.33, conversions: 70},
    {date: '14.06', cost: 80, cpa: 0.2, roi: 220, conversions: 110},
    {date: '15.06', cost: 65, cpa: 0.45, roi: 180, conversions: 95},
    {date: '16.06', cost: 90, cpa: 0.35, roi: 250, conversions: 130},
];

// ✅ Основной компонент
const DashboardChart = ({data}: { data?: typeof DEFAULT_DATA }) => {
    const chartData = data || DEFAULT_DATA;

    return (
        <div className="w-full h-[420px] bg-[#FFF5F5] p-4 rounded-2xl relative border border-pink-100">
            {/* Левая шкала */}
            <div
                className="absolute left-2 top-6 flex flex-col justify-between h-[340px] text-gray-400 text-xs font-medium leading-none">
                <span className="translate-y-[-2px]">Tdy</span>
                <span>0%</span>
                <span>$0</span>
                <span>0</span>
                <span className="translate-y-[2px]">0</span>
            </div>

            <div className="ml-10 h-full">
                <ResponsiveContainer width="100%" height="100%">
                    <ComposedChart data={chartData} margin={{top: 10, right: 20, left: 0, bottom: 10}}>
                        <CartesianGrid stroke="#E5E7EB" strokeDasharray="0" vertical={false} horizontal={true}/>
                        <XAxis
                            dataKey="date"
                            tick={{fill: '#9CA3AF', fontSize: 11, fontWeight: 500}}
                            axisLine={false}
                            tickLine={false}
                            dy={8}
                        />
                        <YAxis hide={true} domain={['auto', 'auto']}/>
                        <Tooltip content={<CustomTooltip/>} cursor={{stroke: '#D1D5DB', strokeWidth: 1}}/>

                        {/* AREA — жёлтая заливка */}
                        <Area
                            type="linear"
                            dataKey="conversions"
                            stroke="none"
                            fill={COLORS.bgArea}
                            fillOpacity={0.4}
                            animationDuration={800}
                        />

                        {/* SPLINE — зелёная плавная линия (ROI) */}
                        <Line
                            type="monotone"
                            dataKey="roi"
                            stroke={COLORS.green}
                            strokeWidth={3}
                            dot={false}
                            activeDot={{r: 6, fill: COLORS.green, stroke: 'white', strokeWidth: 2}}
                            animationDuration={800}
                        />

                        {/* LINE — фиолетовая с точками (Conversions) */}
                        <Line
                            type="linear"
                            dataKey="conversions"
                            stroke={COLORS.purple}
                            strokeWidth={2.5}
                            dot={{r: 4, fill: COLORS.purple, stroke: 'white', strokeWidth: 2}}
                            activeDot={{r: 7, fill: COLORS.purple, stroke: 'white', strokeWidth: 3}}
                            animationDuration={800}
                        />

                        {/* BAR — синие точки (CPA) */}
                        <Line
                            type="linear"
                            dataKey="cpa"
                            stroke={COLORS.blue}
                            strokeWidth={0}
                            dot={{r: 3, fill: COLORS.blue}}
                            activeDot={false}
                            animationDuration={800}
                        />
                    </ComposedChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};

export default DashboardChart;