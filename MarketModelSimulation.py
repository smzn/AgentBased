import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import triang
import pandas as pd

class Customer:
    def __init__(self, customer_id, state='PotentialUser'):
        self.customer_id = customer_id
        self.state = state  # PotentialUser, WantsToBuy, User
        self.delivery_time = 0
        self.usage_time = 0

class MarketModel:
    def __init__(self, initial_customers=5000, ad_effectiveness=0.01, contact_rate=1, 
                 adoption_fraction=0.01, discard_time=6, max_waiting_time=7, max_delivery_time=25):
        self.customers = [Customer(customer_id) for customer_id in range(initial_customers)]
        self.ad_effectiveness = ad_effectiveness
        self.contact_rate = contact_rate
        self.adoption_fraction = adoption_fraction
        self.discard_time = discard_time * 30  # Convert months to days
        self.max_waiting_time = max_waiting_time
        self.max_delivery_time = max_delivery_time
        self.day = 0
        self.history = []

    def update_customers(self):
        for customer in self.customers:
            if customer.state == 'PotentialUser':
                if np.random.rand() < self.ad_effectiveness:
                    customer.state = 'WantsToBuy'
            elif customer.state == 'WantsToBuy':
                delivery_time = np.random.triangular(1, 2, self.max_delivery_time)
                customer.delivery_time = int(delivery_time)
                customer.state = 'User'
            elif customer.state == 'User':
                customer.usage_time += 1
                if customer.usage_time > self.discard_time:
                    customer.state = 'WantsToBuy'
                    customer.usage_time = 0

    def simulate_day(self):
        self.update_customers()
        self.day += 1
        self.record_state()

    def record_state(self):
        states = [c.state for c in self.customers]
        self.history.append({
            'day': self.day,
            'PotentialUser': states.count('PotentialUser'),
            'WantsToBuy': states.count('WantsToBuy'),
            'User': states.count('User')
        })

    def simulate(self, days):
        for _ in range(days):
            self.simulate_day()

    def plot_results(self):
        days = [record['day'] for record in self.history]
        potential_users = [record['PotentialUser'] for record in self.history]
        wants_to_buy = [record['WantsToBuy'] for record in self.history]
        users = [record['User'] for record in self.history]

        df = pd.DataFrame(self.history)
        df.to_csv('history.csv', index=False)

        plt.stackplot(days, potential_users, wants_to_buy, users, labels=['PotentialUser', 'WantsToBuy', 'User'])
        plt.legend(loc='upper right')
        plt.xlabel('Day')
        plt.ylabel('Number of Customers')
        plt.title('Market Model Simulation')
        plt.savefig('market_simulation_stackplot.png')  # グラフをファイルに保存
        plt.show()

        states = ['PotentialUser', 'WantsToBuy', 'User']
        state_colors = ['blue', 'orange', 'green']
        
        plt.figure(figsize=(18, 6))  # グラフのサイズを設定

        for i, state in enumerate(states):
            customer_counts = [record[state] for record in self.history]
            
            plt.subplot(1, 3, i+1)  # 3つのグラフを横に並べる
            plt.plot(days, customer_counts, label=state, color=state_colors[i])
            plt.xlabel('Day')
            plt.ylabel('Number of Customers')
            plt.title(f'{state} Over Time')
            plt.legend()

        plt.tight_layout()  # グラフ同士が重ならないようにレイアウトを調整
        plt.savefig('market_simulation_results.png')  # グラフをファイルに保存
        plt.show()

# シミュレーションの実行
model = MarketModel()
model.simulate(720)  
model.plot_results()
