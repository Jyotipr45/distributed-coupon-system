import concurrent.futures
import time
from collections import Counter

import requests


# ============================================================================
# CONFIGURATION
# ============================================================================

BASE_URL = "http://localhost:8080"

# Change this to the ID of your fresh load-test coupon.
COUPON_ID = 24

# Example:
#   500 requests -> test exact inventory
#   600 requests -> test oversubscription
TOTAL_REQUESTS = 1000

# Number of requests that can execute concurrently.
MAX_WORKERS = 100

REQUEST_TIMEOUT_SECONDS = 30


# ============================================================================
# CLAIM REQUEST
# ============================================================================

def claim_coupon(user_number):

    user_id = f"load-user-{user_number}"

    url = f"{BASE_URL}/api/v1/coupons/{COUPON_ID}/claims"

    payload = {
        "userId": user_id
    }

    start = time.perf_counter()

    try:

        response = requests.post(
            url,
            json=payload,
            timeout=REQUEST_TIMEOUT_SECONDS
        )

        duration = time.perf_counter() - start

        return {
            "user": user_id,
            "status": response.status_code,
            "duration": duration,
            "body": response.text
        }

    except Exception as exception:

        duration = time.perf_counter() - start

        return {
            "user": user_id,
            "status": "ERROR",
            "duration": duration,
            "body": str(exception)
        }


# ============================================================================
# MAIN LOAD TEST
# ============================================================================

def main():

    print("=" * 60)
    print("COUPON CONCURRENCY TEST")
    print("=" * 60)

    print(f"Base URL         : {BASE_URL}")
    print(f"Coupon ID        : {COUPON_ID}")
    print(f"Total requests   : {TOTAL_REQUESTS}")
    print(f"Max workers      : {MAX_WORKERS}")
    print()

    # ------------------------------------------------------------------------
    # Start load test
    # ------------------------------------------------------------------------

    start = time.perf_counter()

    with concurrent.futures.ThreadPoolExecutor(
            max_workers=MAX_WORKERS
    ) as executor:

        results = list(
            executor.map(
                claim_coupon,
                range(1, TOTAL_REQUESTS + 1)
            )
        )

    total_time = time.perf_counter() - start

    # ------------------------------------------------------------------------
    # Count results
    # ------------------------------------------------------------------------

    success = sum(
        1
        for result in results
        if result["status"] == 201
    )

    conflict = sum(
        1
        for result in results
        if result["status"] == 409
    )

    errors = sum(
        1
        for result in results
        if result["status"] == "ERROR"
    )

    other = (
        TOTAL_REQUESTS
        - success
        - conflict
        - errors
    )

    # ------------------------------------------------------------------------
    # Latency calculation
    # ------------------------------------------------------------------------

    durations = [
        result["duration"]
        for result in results
        if isinstance(result["status"], int)
    ]

    average_latency = (
        sum(durations) / len(durations)
        if durations
        else 0
    )

    throughput = (
        TOTAL_REQUESTS / total_time
        if total_time > 0
        else 0
    )

    # ------------------------------------------------------------------------
    # HTTP status distribution
    # ------------------------------------------------------------------------

    status_counts = Counter(
        result["status"]
        for result in results
    )

    # ------------------------------------------------------------------------
    # Main result
    # ------------------------------------------------------------------------

    print()
    print("=" * 60)
    print("RESULT")
    print("=" * 60)

    print(f"Total requests     : {TOTAL_REQUESTS}")
    print(f"Successful         : {success}")
    print(f"Conflict (409)     : {conflict}")
    print(f"Errors             : {errors}")
    print(f"Other responses    : {other}")
    print(f"Total time         : {total_time:.3f} seconds")
    print(f"Average latency    : {average_latency:.3f} seconds")
    print(f"Throughput         : {throughput:.2f} requests/sec")

    # ------------------------------------------------------------------------
    # Status distribution
    # ------------------------------------------------------------------------

    print()
    print("=" * 60)
    print("HTTP STATUS DISTRIBUTION")
    print("=" * 60)

    for status, count in sorted(
            status_counts.items(),
            key=lambda item: str(item[0])
    ):
        print(f"{status}: {count}")

    # ------------------------------------------------------------------------
    # Non-success responses
    # ------------------------------------------------------------------------

    non_success_results = [
        result
        for result in results
        if result["status"] != 201
    ]

    if non_success_results:

        print()
        print("=" * 60)
        print("NON-SUCCESS RESPONSES")
        print("=" * 60)

        # Print at most 20 responses so the terminal doesn't get flooded.
        for result in non_success_results[:20]:

            print(
                f'{result["user"]} -> '
                f'{result["status"]} -> '
                f'{result["body"][:500]}'
            )

        if len(non_success_results) > 20:

            print()
            print(
                f"... and "
                f"{len(non_success_results) - 20} more "
                f"non-success responses."
            )

    # ------------------------------------------------------------------------
    # Final summary
    # ------------------------------------------------------------------------

    print()
    print("=" * 60)
    print("LOAD TEST COMPLETED")
    print("=" * 60)


# ============================================================================
# ENTRY POINT
# ============================================================================

if __name__ == "__main__":
    main()