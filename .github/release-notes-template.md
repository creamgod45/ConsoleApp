## ConsoleApp {{VERSION}}

| 平台 | 安裝包 | 免安裝 |
| --- | --- | --- |
| Linux x64 (Debian/Ubuntu) | `ConsoleApp-{{VERSION}}-linux-x64.deb` | `ConsoleApp-{{VERSION}}-linux-x64-portable.tar.gz` |
| Linux x64 (Fedora/RHEL) | `ConsoleApp-{{VERSION}}-linux-x64.rpm` | 同上 |
| Windows x64 | `ConsoleApp-{{VERSION}}-windows-x64.msi` | `ConsoleApp-{{VERSION}}-windows-x64-portable.zip` |
| macOS Apple Silicon | `ConsoleApp-{{VERSION}}-macos-arm64.dmg` | `ConsoleApp-{{VERSION}}-macos-arm64-portable.tar.gz` |
| macOS Intel | `ConsoleApp-{{VERSION}}-macos-x64.dmg` | `ConsoleApp-{{VERSION}}-macos-x64-portable.tar.gz` |

所有產物都自帶精簡過的 JRE，使用者不需要另外安裝 Java。

### 安裝

```bash
# Debian / Ubuntu
sudo dpkg -i ConsoleApp-{{VERSION}}-linux-x64.deb

# Fedora / RHEL
sudo rpm -i ConsoleApp-{{VERSION}}-linux-x64.rpm

# 免安裝（任何 glibc 夠新的 Linux 發行版）
tar -xzf ConsoleApp-{{VERSION}}-linux-x64-portable.tar.gz
./cg.creamgod.consoleapp/bin/cg.creamgod.consoleapp
```

Windows 直接執行 `.msi`；macOS 開啟 `.dmg` 後把 app 拖到「應用程式」。

### 注意

這些產物未經程式碼簽署。Windows SmartScreen 與 macOS Gatekeeper 會攔下未簽署的安裝包，
macOS 上可用 `xattr -dr com.apple.quarantine <路徑>` 解除隔離後再開啟。

Linux 的 `.deb` / `.rpm` 在 ubuntu-24.04 上建置，需要同等或更新的 glibc。
