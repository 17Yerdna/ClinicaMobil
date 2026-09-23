import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        PlatformModule_iosKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}