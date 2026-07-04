import React, {useContext} from 'react';
import {COLORS} from './config/colors';
import {ChartStateContext} from './context/ChartStateContext';
import {GREEN_LINE_POINTS, POINT_OFFSETS, PURPLE_LINE_POINTS} from './config/coordinates';

// ==================== КАСТОМНЫЕ ТОЧКИ ====================

// Зеленый ромб
export const GreenDiamondDot = ({cx, cy, payload}) => {

    if (cx === null || cy === null || cx === undefined || cy === undefined || !payload || payload.isMidPoint) return null;

    const exactPoint = GREEN_LINE_POINTS[payload.rawDate];
    const exactCx = exactPoint ? exactPoint.cx : cx;
    const exactCy = exactPoint ? exactPoint.cy : cy;

    const size = 3;
    const path = `M${exactCx},${exactCy - size} L${exactCx + size},${exactCy} L${exactCx},${exactCy + size} L${exactCx - size},${exactCy} Z`;

    return (
        <path
            d={path}
            fill={COLORS.green}
            stroke="white"
            strokeWidth={2}
            style={{cursor: 'pointer'}}
            onClick={() => {
                console.log(`🟢 Клик на ромбе (ROI) ${payload.rawDate}: cx=${exactCx}, cy=${exactCy}`);
            }}
        />
    );
};

// Квадратик для фиолетовой линии (Conversions) с отладкой
export const PurpleSquareDot = ({cx, cy, payload, active}) => {

    const {
        selectedDate,
        setSelectedDate,
        hoveredPurple,
        setHoveredPurple,
        setActiveDate,
        activeDate
    } = useContext(ChartStateContext);

    if (cx === null || cy === null || cx === undefined || cy === undefined || !payload || payload.isMidPoint) return null;

    const exactPoint = PURPLE_LINE_POINTS[payload.rawDate];
    const exactCx = exactPoint ? exactPoint.cx : cx;
    const exactCy = exactPoint ? exactPoint.cy : cy;

    if (selectedDate === payload.rawDate) {
        return null;
    }

    // Используем проп active от Recharts или состояние hoveredPurple
    const isActive = active || hoveredPurple === payload.rawDate;

    const size = isActive ? 5 : 8;
    const strokeWidth = isActive ? 2.5 : 0;

    console.log(`🟣 Квадратик ${payload.rawDate}: isActive=${isActive}, hoveredPurple=${hoveredPurple}, activeDate=${activeDate}`);

    return (
        <g
            style={{cursor: 'pointer'}}
            onMouseEnter={() => {
                console.log(`🟣🟣🟣 НАВЕДЕНИЕ НА КВАДРАТИК ${payload.rawDate} 🟣🟣🟣`);
                console.log(`   - Устанавливаем hoveredPurple: ${payload.rawDate}`);
                console.log(`   - Устанавливаем activeDate: ${payload.rawDate}`);
                setHoveredPurple(payload.rawDate);
                setActiveDate(payload.rawDate);
            }}
            onMouseLeave={() => {
                console.log(`🟣🟣🟣 УХОД С КВАДРАТИКА ${payload.rawDate} 🟣🟣🟣`);
                console.log('   - Сбрасываем hoveredPurple и activeDate');
                setHoveredPurple(null);
                setActiveDate(null);
            }}
            onClick={() => {
                console.log(`🟣 Клик на кубике (Conversions) ${payload.rawDate}: cx=${exactCx}, cy=${exactCy}`);
                setSelectedDate(payload.rawDate);
            }}
        >
            <rect
                x={exactCx - size / 2}
                y={exactCy - size / 2}
                width={size}
                height={size}
                fill={COLORS.purple}
                stroke="white"
                strokeWidth={strokeWidth}
                style={{
                    transition: 'all 0.15s ease-in-out',
                    transformOrigin: exactCx + 'px ' + exactCy + 'px',
                }}
            />
        </g>
    );
};

// Желтый круг
export const YellowCircleDot = ({cx, cy, payload}) => {

    if (cx === null || cy === null || cx === undefined || cy === undefined || !payload || payload.isMidPoint) return null;

    const rawDate = payload.rawDate;
    const offset = POINT_OFFSETS.yellow[rawDate] || {cx: 0, cy: 0};
    const finalCx = cx + offset.cx;
    const finalCy = cy + offset.cy;

    return (
        <circle
            cx={finalCx}
            cy={finalCy}
            r={3}
            fill={COLORS.yellowFade}
            stroke="white"
            strokeWidth={2}
            style={{cursor: 'pointer'}}
            onClick={() => {
                console.log(`🟡 Клик на желтой точке ${payload.rawDate}: cx=${finalCx}, cy=${finalCy}`);
            }}
        />
    );
};
