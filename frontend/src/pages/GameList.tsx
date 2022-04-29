import { Grid, SelectChangeEvent } from '@mui/material';
import { useEffect, useState } from 'react';
import GameCard from 'components/GameCard';
import Game from 'model/Game';
import Console from 'model/Console';
import './GameList.sass';
import ConsoleFilter from 'components/ConsoleFilter';
import GameSearchBar from 'components/GameSearchBar';
import { getGames, getConsoles } from 'api/GamesApi';

export default function GameList() {
  const [games, setGames] = useState<Game[]>([])
  const [selectedConsoles, setSelectedConsoles] = useState<string[]>([])
  const [consoles, setConsoles] = useState<Console[]>([])
  const [searchBy, setSearchBy] = useState<string>('')

  function handleConsoleFilterChange (event: SelectChangeEvent<typeof selectedConsoles>) {
    setSelectedConsoles(event.target.value as string[])
  }

  function handleGameSearchChange (event: React.ChangeEvent<HTMLInputElement>) {
    setSearchBy(event.target.value)
  }

  useEffect(() => { 
    getGames(selectedConsoles, searchBy).then(setGames)
  }, [selectedConsoles, searchBy])

  useEffect(() => { 
    getConsoles().then(setConsoles)    
  }, [])

  return (
    <div className='game-list'>
      <div className='game-list-settings'>
        <ConsoleFilter consoles={consoles} selectedConsoles={selectedConsoles} handleChange={handleConsoleFilterChange}/>
        <GameSearchBar handleChange={handleGameSearchChange} />
      </div>
      <div className='game-list-grid'>
        <Grid container spacing={5}>
          {
            games
            .map((game: Game, idx: number) => {
              return (                
                <Grid item key={idx} xs={4}>
                  <GameCard game={game}/>
                </Grid>
              )
            })
          }
        </Grid>
      </div>
    </div>
  );
}
