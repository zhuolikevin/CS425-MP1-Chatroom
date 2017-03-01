import matplotlib.pyplot as plt

n = range(2, 11)

plt.figure(1)
failure_detection_time = [1561.3, 1561.92, 1548, 1555.58, 1525.6, 1573.67, 1558.71, 1551.13, 1559.78]
plt.plot(n, failure_detection_time, "b")
plt.title('Failure Detection Time v.s. Node #')
plt.xlabel('Number of Nodes')
plt.ylabel('Failure Detection Time / ms')
plt.grid(True)

plt.figure(2)
bandwidth = [19.99, 59.9, 120, 200, 300, 420.25, 560.85, 721.2, 901.3]
plt.plot(n, bandwidth, "b")
plt.title('System Bandwidth v.s. Node #')
plt.xlabel('Number of Nodes')
plt.ylabel('System Bandwidth / msg/s')
plt.grid(True)
plt.show()
