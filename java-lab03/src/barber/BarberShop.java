package barber;

import java.util.concurrent.Semaphore;

enum HairType {
    LONG("long"),
    SHORT("short"),
    BALD("bald hehe");

    private final String name;

    HairType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

class Client implements Runnable {
    private final int id;
    private final BarberShop barberShop;

    HairType hairType;

    Client(BarberShop barberShop, int id, HairType hairType) {
        this.barberShop = barberShop;
        this.id = id;
        this.hairType = hairType;
        System.out.println("Client " + this + " created");
    }

    @Override
    public String toString() {
        return "Client with ID=" + this.id + " and " + this.hairType + " haircut";
    }

    public void run() {
        try {
            barberShop.protection.acquire();
            if (barberShop.freeSeats > 0) {
                barberShop.freeSeats--;
                System.out.println(this + " joined barber shop");

                if (barberShop.sleeps) {
                    barberShop.sleeps = false;
                    barberShop.protection.release();
                    barberShop.armchair.release();
                } else {
                    barberShop.protection.release();
                }

                barberShop.waitingRoom.acquire();   // Critical section acquisition
                barberShop.currentClient = this;
                barberShop.availableClient.release();
            } else {
                System.out.println(this + " left barber shop");
                barberShop.protection.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}

class Barber implements Runnable {
    private final BarberShop barberShop;

    Barber(BarberShop barberShop) {
        this.barberShop = barberShop;
    }

    private void cut(Client client) throws InterruptedException {
        System.out.println("Cutting " + client);
        switch (client.hairType) {
            case LONG: client.hairType = HairType.SHORT; break;
            case SHORT: client.hairType = HairType.BALD; break;
            case BALD: client.hairType = HairType.BALD; break;
        }
        Thread.sleep(1000);
        System.out.println("Obtained " + client);
    }

    public void run() {
        while(true) {
            try {
                barberShop.protection.acquire();
                // If there are clients awaiting
                if (barberShop.freeSeats < barberShop.seatsNumber) {
                    barberShop.waitingRoom.release(); // Critical section delivery
                    barberShop.availableClient.acquire();
                    cut(barberShop.currentClient);
                    barberShop.freeSeats++;
                    barberShop.protection.release();
                } else {
                    barberShop.sleeps = true;
                    barberShop.protection.release();
                    barberShop.armchair.acquire();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class BarberShop {
    final int seatsNumber;
    final Semaphore waitingRoom;
    final Semaphore protection;
    final Semaphore armchair;
    final Semaphore availableClient;

    // Mutable fields that ought to be protected.
    int freeSeats;
    boolean sleeps;
    Client currentClient;

    BarberShop(int seatsNumber) {
        this.seatsNumber = seatsNumber;
        this.waitingRoom = new Semaphore(0);
        this.protection = new Semaphore(1);
        this.armchair = new Semaphore(0, true);
        this.availableClient = new Semaphore(0);

        this.freeSeats = seatsNumber;
        this.currentClient = null;

        Thread barber = new Thread(new Barber(this));
        barber.start();
    }


    static public void main(String[] args) {
        BarberShop barberShop = new BarberShop(5);

        final int n = 10;
        Thread[] clients = new Thread[n];
        for(int i=0; i<n; i++) {
            HairType hairType;
            switch(i % 3) {
                case 0: hairType = HairType.LONG; break;
                case 1: hairType = HairType.SHORT; break;
                case 2: hairType = HairType.BALD; break;
                default: hairType = HairType.BALD; break;
            }
            clients[i] = new Thread(new Client(barberShop, i, hairType));
            clients[i].start();
        }
    }
}
