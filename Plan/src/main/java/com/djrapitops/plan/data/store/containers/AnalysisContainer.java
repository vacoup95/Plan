package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.CommandUseMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.html.graphs.line.*;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.structure.RecentLoginList;
import com.djrapitops.plan.utilities.html.structure.SessionAccordion;
import com.djrapitops.plan.utilities.html.tables.CommandUseTable;
import com.djrapitops.plan.utilities.html.tables.PlayersTable;
import com.djrapitops.plan.utilities.html.tables.ServerSessionTable;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Container used for analysis.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.keys.AnalysisKeys for Key objects
 * @see com.djrapitops.plan.data.store.PlaceholderKey for placeholder information
 */
public class AnalysisContainer extends DataContainer {

    private final ServerContainer serverContainer;

    public AnalysisContainer(ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
        addAnalysisSuppliers();
    }

    private void addAnalysisSuppliers() {
        putSupplier(AnalysisKeys.SESSIONS_MUTATOR, () -> SessionsMutator.forContainer(serverContainer));
        putSupplier(AnalysisKeys.TPS_MUTATOR, () -> TPSMutator.forContainer(serverContainer));

        addConstants();
        addPlayerSuppliers();
        addSessionSuppliers();
        addGraphSuppliers();
        addTPSAverageSuppliers();
        addCommandSuppliers();
    }

    private void addConstants() {
        long now = System.currentTimeMillis();
        putRawData(AnalysisKeys.ANALYSIS_TIME, now);
        putRawData(AnalysisKeys.ANALYSIS_TIME_DAY_AGO, now - TimeAmount.DAY.ms());
        putRawData(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO, now - TimeAmount.WEEK.ms());
        putRawData(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO, now - TimeAmount.MONTH.ms());
        putSupplier(AnalysisKeys.REFRESH_TIME_F, () -> Formatters.second().apply(() -> getUnsafe(AnalysisKeys.ANALYSIS_TIME)));

        putRawData(AnalysisKeys.VERSION, PlanPlugin.getInstance().getVersion());
        putSupplier(AnalysisKeys.TIME_ZONE, MiscUtils::getTimeZoneOffsetHours);
        putRawData(AnalysisKeys.FIRST_DAY, 1);
        putRawData(AnalysisKeys.TPS_MEDIUM, Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber());
        putRawData(AnalysisKeys.TPS_HIGH, Settings.THEME_GRAPH_TPS_THRESHOLD_HIGH.getNumber());

        addServerProperties();
        addThemeColors();
    }

    private void addServerProperties() {
        putSupplier(AnalysisKeys.SERVER_NAME, ServerInfo::getServerName);

        ServerProperties serverProperties = ServerInfo.getServerProperties();
        putRawData(AnalysisKeys.PLAYERS_MAX, serverProperties.getMaxPlayers());
        putRawData(AnalysisKeys.PLAYERS_ONLINE, serverProperties.getOnlinePlayers());
    }

    private void addThemeColors() {
        putRawData(AnalysisKeys.ACTIVITY_PIE_COLORS, Theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE));
        putRawData(AnalysisKeys.GM_PIE_COLORS, Theme.getValue(ThemeVal.GRAPH_GM_PIE));
        putRawData(AnalysisKeys.PLAYERS_GRAPH_COLOR, Theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
        putRawData(AnalysisKeys.TPS_LOW_COLOR, Theme.getValue(ThemeVal.GRAPH_TPS_LOW));
        putRawData(AnalysisKeys.TPS_MEDIUM_COLOR, Theme.getValue(ThemeVal.GRAPH_TPS_MED));
        putRawData(AnalysisKeys.TPS_HIGH_COLOR, Theme.getValue(ThemeVal.GRAPH_TPS_HIGH));
        putRawData(AnalysisKeys.WORLD_MAP_LOW_COLOR, Theme.getValue(ThemeVal.WORLD_MAP_LOW));
        putRawData(AnalysisKeys.WORLD_MAP_HIGH_COLOR, Theme.getValue(ThemeVal.WORLD_MAP_HIGH));
        putRawData(AnalysisKeys.WORLD_PIE_COLORS, Theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
    }

    private void addPlayerSuppliers() {
        putSupplier(AnalysisKeys.PLAYER_NAMES, () -> serverContainer.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>())
                .stream().collect(Collectors.toMap(
                        p -> p.getUnsafe(PlayerKeys.UUID), p -> p.getValue(PlayerKeys.NAME).orElse("?"))
                )
        );
        putSupplier(AnalysisKeys.PLAYERS_TOTAL, () -> serverContainer.getValue(ServerKeys.PLAYER_COUNT).orElse(0));
        putSupplier(AnalysisKeys.PLAYERS_LAST_PEAK, () ->
                serverContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS)
                        .map(dateObj -> Integer.toString(dateObj.getValue())).orElse("-")
        );
        putSupplier(AnalysisKeys.PLAYERS_ALL_TIME_PEAK, () ->
                serverContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS)
                        .map(dateObj -> Integer.toString(dateObj.getValue())).orElse("-")
        );
        putSupplier(AnalysisKeys.LAST_PEAK_TIME_F, () ->
                serverContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS)
                        .map(dateObj -> Formatters.year().apply(dateObj)).orElse("-")
        );
        putSupplier(AnalysisKeys.ALL_TIME_PEAK_TIME_F, () ->
                serverContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS)
                        .map(dateObj -> Formatters.year().apply(dateObj)).orElse("-")
        );
        putSupplier(AnalysisKeys.OPERATORS, () -> serverContainer.getValue(ServerKeys.OPERATORS).map(List::size).orElse(0));
        putSupplier(AnalysisKeys.PLAYERS_TABLE, () ->
                PlayersTable.forServerPage(serverContainer.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>())).parseHtml()
        );
    }

    private void addSessionSuppliers() {
        putSupplier(AnalysisKeys.SESSION_ACCORDION, () -> SessionAccordion.forServer(
                getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).all(),
                () -> Database.getActive().fetch().getServerNames(),
                () -> getUnsafe(AnalysisKeys.PLAYER_NAMES)
        ));
        putSupplier(AnalysisKeys.RECENT_LOGINS, () -> new RecentLoginList(
                        serverContainer.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>())
                ).toHtml()
        );
        putSupplier(AnalysisKeys.SESSION_TABLE, () -> new ServerSessionTable(
                getUnsafe(AnalysisKeys.PLAYER_NAMES), getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).all()).parseHtml()
        );
        putSupplier(AnalysisKeys.SESSION_ACCORDION_HTML, () -> getUnsafe(AnalysisKeys.SESSION_ACCORDION).toHtml());
        putSupplier(AnalysisKeys.SESSION_ACCORDION_FUNCTIONS, () -> getUnsafe(AnalysisKeys.SESSION_ACCORDION).toViewScript());

        putSupplier(AnalysisKeys.AVERAGE_SESSION_LENGTH_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toAverageSessionLength())
        );
        putSupplier(AnalysisKeys.SESSION_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).count());
        putSupplier(AnalysisKeys.PLAYTIME_TOTAL, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toPlaytime());
        putSupplier(AnalysisKeys.DEATHS, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toDeathCount());
        putSupplier(AnalysisKeys.MOB_KILL_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toMobKillCount());
        putSupplier(AnalysisKeys.PLAYER_KILL_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toPlayerKillCount());
        putSupplier(AnalysisKeys.PLAYTIME_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.PLAYTIME_TOTAL))
        );
        putSupplier(AnalysisKeys.AVERAGE_PLAYTIME_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.PLAYTIME_TOTAL) / (long) getUnsafe(AnalysisKeys.PLAYERS_TOTAL))
        );
        putSupplier(AnalysisKeys.AVERAGE_SESSION_LENGTH_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toAverageSessionLength())
        );
    }

    private void addGraphSuppliers() {
        Key<WorldPie> worldPie = new Key<>(WorldPie.class, "WORLD_PIE");
        putSupplier(worldPie, () -> new WorldPie(serverContainer.getValue(ServerKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()))));
        putSupplier(AnalysisKeys.WORLD_PIE_SERIES, () -> getUnsafe(worldPie).toHighChartsSeries());
        putSupplier(AnalysisKeys.GM_PIE_SERIES, () -> getUnsafe(worldPie).toHighChartsDrilldown());
        putSupplier(AnalysisKeys.PLAYERS_ONLINE_SERIES, () ->
                new OnlineActivityGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries()
        );
        putSupplier(AnalysisKeys.TPS_SERIES, () -> new TPSGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.CPU_SERIES, () -> new CPUGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.RAM_SERIES, () -> new RamGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.ENTITY_SERIES, () -> new EntityGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.CHUNK_SERIES, () -> new ChunkGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
    }

    private void addTPSAverageSuppliers() {
        Key<TPSMutator> tpsMonth = new Key<>(TPSMutator.class, "TPS_MONTH");
        Key<TPSMutator> tpsWeek = new Key<>(TPSMutator.class, "TPS_WEEK");
        Key<TPSMutator> tpsDay = new Key<>(TPSMutator.class, "TPS_DAY");

        putSupplier(tpsMonth, () -> TPSMutator.copyOf(getUnsafe(AnalysisKeys.TPS_MUTATOR))
                .filterDataBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(tpsWeek, () -> TPSMutator.copyOf(getUnsafe(AnalysisKeys.TPS_MUTATOR))
                .filterDataBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(tpsDay, () -> TPSMutator.copyOf(getUnsafe(AnalysisKeys.TPS_MUTATOR))
                .filterDataBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_DAY_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );

        putSupplier(AnalysisKeys.TPS_SPIKE_MONTH, () -> getUnsafe(tpsMonth).lowTpsSpikeCount());
        putSupplier(AnalysisKeys.AVG_TPS_MONTH, () -> getUnsafe(tpsMonth).averageTPS());
        putSupplier(AnalysisKeys.AVG_CPU_MONTH, () -> getUnsafe(tpsMonth).averageCPU());
        putSupplier(AnalysisKeys.AVG_RAM_MONTH, () -> getUnsafe(tpsMonth).averageRAM());
        putSupplier(AnalysisKeys.AVG_ENTITY_MONTH, () -> getUnsafe(tpsMonth).averageEntities());
        putSupplier(AnalysisKeys.AVG_CHUNK_MONTH, () -> getUnsafe(tpsMonth).averageChunks());
        putSupplier(AnalysisKeys.TPS_SPIKE_WEEK, () -> getUnsafe(tpsWeek).lowTpsSpikeCount());
        putSupplier(AnalysisKeys.AVG_TPS_WEEK, () -> getUnsafe(tpsWeek).averageTPS());
        putSupplier(AnalysisKeys.AVG_CPU_WEEK, () -> getUnsafe(tpsWeek).averageCPU());
        putSupplier(AnalysisKeys.AVG_RAM_WEEK, () -> getUnsafe(tpsWeek).averageRAM());
        putSupplier(AnalysisKeys.AVG_ENTITY_WEEK, () -> getUnsafe(tpsWeek).averageEntities());
        putSupplier(AnalysisKeys.AVG_CHUNK_WEEK, () -> getUnsafe(tpsWeek).averageChunks());
        putSupplier(AnalysisKeys.TPS_SPIKE_DAY, () -> getUnsafe(tpsDay).lowTpsSpikeCount());
        putSupplier(AnalysisKeys.AVG_TPS_DAY, () -> getUnsafe(tpsDay).averageTPS());
        putSupplier(AnalysisKeys.AVG_CPU_DAY, () -> getUnsafe(tpsDay).averageCPU());
        putSupplier(AnalysisKeys.AVG_RAM_DAY, () -> getUnsafe(tpsDay).averageRAM());
        putSupplier(AnalysisKeys.AVG_ENTITY_DAY, () -> getUnsafe(tpsDay).averageEntities());
        putSupplier(AnalysisKeys.AVG_CHUNK_DAY, () -> getUnsafe(tpsDay).averageChunks());
    }

    private void addCommandSuppliers() {
        putSupplier(AnalysisKeys.COMMAND_USAGE_TABLE, () -> new CommandUseTable(serverContainer).parseHtml());
        putSupplier(AnalysisKeys.COMMAND_COUNT_UNIQUE, () -> serverContainer.getValue(ServerKeys.COMMAND_USAGE).map(Map::size).orElse(0));
        putSupplier(AnalysisKeys.COMMAND_COUNT, () -> CommandUseMutator.forContainer(serverContainer).commandUsageCount());
    }
}