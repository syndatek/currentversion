import numpy as np
import matplotlib

# Required for Android / Chaquopy
matplotlib.use("Agg")

import matplotlib.pyplot as plt
from scipy.signal import butter, filtfilt

# ==========================================
# Configuration
# ==========================================
Fs = 1000
SCALE = 0.000286
GAIN = 1


# ==========================================
# ECG Bandpass Filter
# ==========================================
def bandpass_filter(signal_data,
                    lowcut=0.5,
                    highcut=40.0,
                    fs=1000,
                    order=4):
    nyquist = 0.5 * fs

    low = lowcut / nyquist
    high = highcut / nyquist

    b, a = butter(
        order,
        [low, high],
        btype="band"
    )

    return filtfilt(
        b,
        a,
        signal_data
    )


# ==========================================
# Lead4 -> V2 Analysis
# ==========================================
def analyzeLead4(samples, graph_path):
    try:

        print("================================")
        print("analyzeLead4 called")
        print("graph_path =", graph_path)
        print("samples type =", type(samples))
        print("================================")

        # ----------------------------------
        # Convert Java ArrayList
        # ----------------------------------
        Lead4_list = []

        try:

            size = samples.size()

            print("ArrayList size =", size)

            for i in range(size):
                Lead4_list.append(
                    float(samples.get(i))
                )

        except Exception:

            # Already Python list
            Lead4_list = [
                float(x)
                for x in samples
            ]

        # ----------------------------------
        # Convert to NumPy
        # ----------------------------------
        Lead4 = np.array(
            Lead4_list,
            dtype=np.float64
        )

        print(
            "Received samples =",
            len(Lead4)
        )

        if len(Lead4) == 0:
            return [
                0.0,
                ""
            ]

        print(
            "Min =",
            np.min(Lead4)
        )

        print(
            "Max =",
            np.max(Lead4)
        )

        # ----------------------------------
        # Lead4 -> V2 Conversion
        # ----------------------------------
        V2 = (
                Lead4 *
                SCALE *
                (-GAIN)
        )

        # ----------------------------------
        # Bandpass Filter
        # ----------------------------------
        V2_filtered = bandpass_filter(
            V2,
            fs=Fs
        )

        # ----------------------------------
        # Time Axis
        # ----------------------------------
        t = np.arange(
            len(V2_filtered)
        ) / Fs

        # ----------------------------------
        # Plot ECG
        # ----------------------------------
        plt.figure(
            figsize=(15, 4)
        )

        plt.plot(
            t,
            V2_filtered,
            linewidth=0.8
        )

        plt.title(
            "Lead4 Used As V2"
        )

        plt.xlabel(
            "Time (Seconds)"
        )

        plt.ylabel(
            "Amplitude (mV)"
        )

        plt.grid(True)

        plt.tight_layout()

        # ----------------------------------
        # Save Graph
        # ----------------------------------
        plt.savefig(
            graph_path,
            dpi=300,
            bbox_inches="tight"
        )

        plt.close()

        print(
            "Graph saved =",
            graph_path
        )

        # ----------------------------------
        # HH Placeholder
        # ----------------------------------
        hh_ms = 0.0

        result = [
            float(hh_ms),
            str(graph_path)
        ]

        print(
            "Returning =",
            result
        )

        return result

    except Exception as e:

        import traceback

        print("================================")
        print("PYTHON ERROR")
        print(str(e))
        traceback.print_exc()
        print("================================")

        return [
            0.0,
            ""
        ]
