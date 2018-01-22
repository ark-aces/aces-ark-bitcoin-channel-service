Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/xenial64"
  config.vm.provider "virtualbox" do |v|
    v.memory = 4048
    v.cpus = 2
  end
  
  # postgresql
  config.vm.network "forwarded_port", guest: 5432, host: 5432
  
  # bitcoind rcp (18332 for testnet, 8332 for mainnet)
  config.vm.network "forwarded_port", guest: 18332, host: 18332
  
  # ark node api
  config.vm.network "forwarded_port", guest: 4001, host: 4001
end
