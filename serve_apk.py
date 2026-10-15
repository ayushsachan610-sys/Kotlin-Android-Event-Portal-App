#!/usr/bin/env python3
"""
Zenith '26 - Mobile APK Distribution Server
Serves the Android APK over your local Wi-Fi network so you can
download and install it directly on your Android phone using your mobile browser.
"""

import http.server
import socket
import socketserver
import sys
from pathlib import Path

# Ensure UTF-8 output on Windows consoles
if sys.stdout and hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8082
PROJECT_DIR = Path(__file__).resolve().parent
APK_SEARCH_PATHS = [
    PROJECT_DIR / "android" / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk",
    PROJECT_DIR / "app-debug.apk",
]


def get_local_ip():
    """Finds the laptop's local LAN IPv4 address."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    except Exception:
        ip = "127.0.0.1"
    finally:
        s.close()
    return ip


def find_apk():
    for p in APK_SEARCH_PATHS:
        if p.is_file():
            return p
    return None


class APKRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        apk_file = find_apk()
        if self.path == "/" or self.path == "/index.html":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.end_headers()

            apk_status = (
                f'<p style="color: #10b981; font-weight: bold;">[Ready] Found APK: {apk_file.name} ({apk_file.stat().st_size / (1024*1024):.1f} MB)</p>'
                f'<a href="/app-debug.apk" style="display: inline-block; padding: 14px 28px; background: linear-gradient(135deg, #7c3aed, #c026d3); color: white; text-decoration: none; border-radius: 12px; font-weight: bold; font-size: 16px; margin-top: 10px;">Download Zenith \'26 APK</a>'
                if apk_file
                else '<p style="color: #f43f5e; font-weight: bold;">[Pending] app-debug.apk not built yet.<br><br>In Android Studio, click <strong>Build &gt; Build Bundle(s) / APK(s) &gt; Build APK(s)</strong></p>'
            )

            html = f"""<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zenith '26 — Download APK</title>
    <style>
        body {{
            background-color: #0f172a;
            color: #f8fafc;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            text-align: center;
            padding: 40px 20px;
        }}
        .card {{
            max-width: 480px;
            margin: 0 auto;
            background: #1e293b;
            border: 1px solid #334155;
            border-radius: 20px;
            padding: 30px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
        }}
        h1 {{ margin-top: 0; color: #a78bfa; font-size: 24px; }}
        p {{ color: #94a3b8; font-size: 14px; line-height: 1.6; }}
        .badge {{ background: #2e1065; color: #c4b5fd; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: bold; }}
        code {{ background: #0f172a; color: #a78bfa; padding: 2px 6px; border-radius: 6px; }}
    </style>
</head>
<body>
    <div class="card">
        <div style="font-size: 40px; margin-bottom: 10px;">⚡</div>
        <h1>ZENITH '26</h1>
        <span class="badge">Native Android Mobile App</span>
        <p>Install the Zenith '26 College Fest Management System directly onto your Android phone.</p>
        <hr style="border: 0; border-top: 1px solid #334155; margin: 20px 0;">
        {apk_status}
        <div style="margin-top: 30px; text-align: left; background: #0f172a; padding: 16px; border-radius: 12px; border: 1px solid #334155;">
            <p style="margin: 0 0 6px 0; font-weight: bold; color: #cbd5e1;">Testing Steps on Phone:</p>
            <ol style="margin: 0; padding-left: 20px; font-size: 13px; color: #94a3b8; line-height: 1.6;">
                <li>Download &amp; open the APK on your phone.</li>
                <li>Allow "Install from Unknown Sources" if prompted.</li>
                <li>Ensure phone is connected to the same Wi-Fi.</li>
                <li>Backend server address: <code>http://{get_local_ip()}:8000/</code></li>
            </ol>
        </div>
    </div>
</body>
</html>"""
            self.wfile.write(html.encode("utf-8"))
            return

        elif self.path == "/app-debug.apk":
            if apk_file and apk_file.is_file():
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="zenith26-app-debug.apk"')
                self.send_header("Content-Length", str(apk_file.stat().st_size))
                self.end_headers()
                with open(apk_file, "rb") as f:
                    self.wfile.write(f.read())
                return
            else:
                self.send_error(404, "APK not found. Please build it in Android Studio first.")
                return

        return super().do_GET()


def main():
    local_ip = get_local_ip()
    apk_file = find_apk()
    print("=" * 70)
    print("ZENITH '26 -- MOBILE APK DOWNLOAD SERVER")
    print("=" * 70)
    print(f"[*] Local Machine IP Address : {local_ip}")
    print(f"[*] Distribution Web URL     : http://{local_ip}:{PORT}/")
    print(f"[*] Direct APK Download Link : http://{local_ip}:{PORT}/app-debug.apk")
    print("-" * 70)
    if apk_file:
        print(f"[+] Found compiled APK at     : {apk_file}")
    else:
        print("[!] APK file not found in outputs yet.")
        print("    In Android Studio, click: Build > Build Bundle(s) / APK(s) > Build APK(s)")
    print("-" * 70)
    print(f"[*] Open http://{local_ip}:{PORT}/ on your Android phone's browser to download!")
    print("[*] Press Ctrl+C to stop this server.\n")

    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("0.0.0.0", PORT), APKRequestHandler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n[!] APK server stopped.")


if __name__ == "__main__":
    main()
