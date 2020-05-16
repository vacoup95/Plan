/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.commands;

import com.djrapitops.plan.commands.subcommands.*;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * TreeCommand for the /plan command, and all SubCommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 */
@Singleton
public class OldPlanCommand extends TreeCmdNode {

    private final QInspectCommand qInspectCommand;
    private final SearchCommand searchCommand;
    private final Lazy<WebUserCommand> webUserCommand;
    private final InfoCommand infoCommand;
    private final ReloadCommand reloadCommand;
    private final Lazy<ManageCommand> manageCommand;

    private boolean commandsRegistered;

    @Inject
    public OldPlanCommand(
            ColorScheme colorScheme,
            Locale locale,
            // Group 1
            QInspectCommand qInspectCommand,
            SearchCommand searchCommand,
            // Group 2
            Lazy<WebUserCommand> webUserCommand,
            // Group 3
            InfoCommand infoCommand,
            ReloadCommand reloadCommand,
            Lazy<ManageCommand> manageCommand
    ) {
        super("plan", "", CommandType.CONSOLE, null);

        commandsRegistered = false;

        this.qInspectCommand = qInspectCommand;
        this.searchCommand = searchCommand;
        this.webUserCommand = webUserCommand;
        this.infoCommand = infoCommand;
        this.reloadCommand = reloadCommand;
        this.manageCommand = manageCommand;

        getHelpCommand().setPermission(Permissions.HELP.getPermission());
        setDefaultCommand("inspect");
        setColorScheme(colorScheme);
        setInDepthHelp(locale.getArray(DeepHelpLang.PLAN));
    }

    public void registerCommands() {
        if (commandsRegistered) {
            return;
        }

        CommandNode[] analyticsGroup = {
                qInspectCommand,
                searchCommand
        };
        CommandNode[] webGroup = {
                webUserCommand.get()
        };
        CommandNode[] manageGroup = {
                infoCommand,
                reloadCommand,
                manageCommand.get()
        };
        setNodeGroups(analyticsGroup, webGroup, manageGroup);
        commandsRegistered = true;
    }
}