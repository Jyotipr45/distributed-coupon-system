import time
import requests


BASE_URL = "http://localhost:8080"

METRICS = [
    "hikaricp.connections.active",
    "hikaricp.connections.pending",
    "hikaricp.connections.idle",
]


def get_metric(metric_name):

    url = f"{BASE_URL}/actuator/metrics/{metric_name}"

    response = requests.get(
        url,
        timeout=5
    )

    if response.status_code == 404:
        return None

    response.raise_for_status()

    data = response.json()

    return data["measurements"][0]["value"]


def format_value(value):

    if value is None:
        return "N/A"

    return f"{value:.2f}"


def main():

    print("=" * 75)
    print("COUPON SYSTEM - HIKARI LIVE MONITOR")
    print("=" * 75)

    print(
        f"{'TIME':<12}"
        f"{'ACTIVE':<12}"
        f"{'PENDING':<12}"
        f"{'IDLE':<12}"
    )

    print("-" * 75)

    while True:

        try:

            active = get_metric(
                "hikaricp.connections.active"
            )

            pending = get_metric(
                "hikaricp.connections.pending"
            )

            idle = get_metric(
                "hikaricp.connections.idle"
            )

            current_time = time.strftime("%H:%M:%S")

            print(
                f"{current_time:<12}"
                f"{format_value(active):<12}"
                f"{format_value(pending):<12}"
                f"{format_value(idle):<12}"
            )

            time.sleep(0.5)

        except KeyboardInterrupt:

            print("\nMonitoring stopped.")
            break

        except Exception as exception:

            print(f"Monitor error: {exception}")
            time.sleep(1)


if __name__ == "__main__":
    main()