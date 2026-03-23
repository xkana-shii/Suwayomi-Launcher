package suwayomi.tachidesk.launcher.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.LauncherViewModel
import suwayomi.tachidesk.launcher.ServerUpdater
import suwayomi.tachidesk.launcher.actions
import suwayomi.tachidesk.launcher.bind
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jbutton
import suwayomi.tachidesk.launcher.jpanel
import java.nio.file.Paths
import javax.swing.JOptionPane

@Suppress("ktlint:standard:function-naming")
fun ServerUpdate(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Update Suwayomi-Server.jar") {
        isEditable = false
    }.bind(CC().spanX().wrap())

    jbutton("Update Server .jar from GitHub Release") {
        toolTipText = "Downloads and replaces the Suwayomi-Server.jar with the latest release from GitHub"
        actions()
            .onEach {
                isEnabled = false
                val serverJarPath = Paths.get(vm.rootDir.value ?: ".", "Suwayomi-Server.jar")
                scope.launch(Dispatchers.IO) {
                    val result = ServerUpdater.updateServerJar(serverJarPath)
                    launch(Dispatchers.Swing) {
                        if (result.isSuccess) {
                            JOptionPane.showMessageDialog(this@jbutton, "Update successful!\nSaved to $serverJarPath")
                        } else {
                            JOptionPane.showMessageDialog(this@jbutton, "Update failed: ${result.exceptionOrNull()?.message}")
                        }
                        isEnabled = true
                    }
                }
            }.launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
}
