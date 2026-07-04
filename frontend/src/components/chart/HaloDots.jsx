import { useContext } from 'react';
import { ActiveDateContext } from '../DashboardChart';
import { COLORS } from '../config/colors';
import { GREEN_LINE_POINTS, PURPLE_LINE_POINTS } from '../config/coordinates';

// ==================== HALO-ЭФФЕКТЫ С ОТЛАДКОЙ ====================

export const YellowHaloDot = (props) => {
    const {cx, cy, payload} = props;
    const activeDate = useContext(ActiveDateContext);

    const isActive = payload?.rawDate === activeDate;
    console.log(`🟡 YellowHaloDot: ${payload?.rawDate}, activeDate=${activeDate}, isActive=${isActive}`);

    if (cx == null || cy == null || !payload || payload.isMidPoint) return null;
    if (payload.rawDate !== activeDate) return null;

    console.log(`✅🟡 YellowHaloDot ОТОБРАЖЕН для ${payload.rawDate}`);

    return (
        <circle
            cx={cx}
            cy={cy}
            r={14}
            fill={COLORS.yellowFade}
            opacity={0.6}
            style={{mixBlendMode: 'multiply'}}
        />
    );
};

export const GreenHaloDot = (props) => {
    const {cx, cy, payload} = props;
    const activeDate = useContext(ActiveDateContext);

    const isActive = payload?.rawDate === activeDate;
    console.log(`🟢 GreenHaloDot: ${payload?.rawDate}, activeDate=${activeDate}, isActive=${isActive}`);

    if (cx == null || cy == null || !payload || payload.isMidPoint) return null;
    if (payload.rawDate !== activeDate) return null;

    const exactPoint = GREEN_LINE_POINTS[payload.rawDate];
    const exactCx = exactPoint ? exactPoint.cx : cx;
    const exactCy = exactPoint ? exactPoint.cy : cy;

    console.log(`✅🟢 GreenHaloDot ОТОБРАЖЕН для ${payload.rawDate} на координатах cx=${exactCx}, cy=${exactCy}`);

    return (
        <circle
            cx={exactCx}
            cy={exactCy}
            r={14}
            fill={COLORS.greenFade}
            opacity={0.7}
            style={{mixBlendMode: 'multiply'}}
        />
    );
};

// Хало-эффект для фиолетовой линии (Conversions) - с поддержкой activeDate из пропсов
export const PurpleHaloDot = (props) => {
    const {cx, cy, payload, activeDate: propActiveDate} = props;
    const contextActiveDate = useContext(ActiveDateContext);

    // Используем activeDate из пропсов, если передано, иначе из контекста
    const currentActiveDate = propActiveDate !== undefined ? propActiveDate : contextActiveDate;

    if (cx == null || cy == null || !payload || payload.isMidPoint) return null;
    if (payload.rawDate !== currentActiveDate) return null;

    const exactPoint = PURPLE_LINE_POINTS[payload.rawDate];
    const exactCx = exactPoint ? exactPoint.cx : cx;
    const exactCy = exactPoint ? exactPoint.cy : cy;

    console.log(`✅🟣 PurpleHaloDot ОТОБРАЖЕН для ${payload.rawDate} на координатах cx=${exactCx}, cy=${exactCy}`);

    return (
        <circle
            cx={exactCx}
            cy={exactCy}
            r={14}
            fill={COLORS.purpleFade}
            opacity={0.6}
            style={{mixBlendMode: 'multiply'}}
        />
    );
};
